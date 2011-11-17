package es.nextiraone.ing.sudoku.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Solutions implements Iterator<Sudoku> {

	/** Iterador sobre las soluciones de un Sudoku.
     */

    // el sudoku raiz que estamos resolviendo
    private Sudoku root;
    // La rama de sudokus "hijos" que estamos explorando
    private Solutions branch;
    // el elemento pivote que hemos elegido para resolver
    private int pivot;
    // los valores de pivote que quedan por probar
    private List<Integer> values;
    // true cuando ya no queden pivote, ni branches.
    private boolean done;
    // cantidad de rutas sin solucion que hemos encontrado
    private int deadends;

    public Solutions(Sudoku root) {
    	/// Construye el iterador
    	this.root     = root;
    	this.branch   = null;
    	this.values   = new ArrayList<Integer>();
    	this.done     = false;
    	this.deadends = 0;
    	/* busco celdas que no tengan todos los valores fijos.
    	 */
    	List<Integer> free = new Sorter(root).free();
   		if(free.size() > 0) {
   			pivot = free.get(0);
   			for(int val: Cache.OPT[root.getAt(pivot)]) {
   				values.add(val+1);
   			}
   			/* Randomizo los valores para que las soluciones
   			 * no salgan siempre iguales.
   			 */
   			Collections.shuffle(values);
   		}
   	}

    public int getDeadends() {
    	return deadends;
    }

    private void replaceBranch() {
    	/// Busca un nuevo branch con soluciones.
		if(branch != null) {
			deadends += branch.deadends;
		}
		branch = null;
		for(;branch == null && values.size() > 0;) {
			Sudoku tmp = new Sudoku(root);
			try {
				tmp.fix(pivot, values.remove(0));
				tmp.heuristic();
				branch = new Solutions(tmp);
				if(!branch.hasNext()) {
					deadends += branch.deadends;
					branch = null;
				}
			}
			catch(DeadEndException exc) {
				/* Fijar la celda ha provocado un error,
				 * esta rama no me vale.
				 */
				deadends += 1;
				branch = null;
			}
		}
		if(branch == null) {
			/* No hemos podido reemplazar la rama. Marcamos
			 * el fin de la busqueda.
			 */
			done = true;
		}
    }

	@Override
	public boolean hasNext() {
		if(branch == null && values.size() == 0) {
			/* no hay branches, este sudoku no tenia pivot,
			 * o ya se han agotado todas las opciones del pivot.
			 */
			return !done;
		}
		if(branch == null || !branch.hasNext()) {
			/* No hemos inicializado el sudoku, o hemos agotado
			 * la rama. Buscamos otra rama que tenga soluciones.
			 */
			replaceBranch();
		}
		return (branch != null);
	}

	@Override
	public Sudoku next() {
		/// Obtiene la siguiente solucion
		/* Como a esta funcion se le llama despues de hasNext(),
		 * se debe cumplir que:
		 * - Si branch == null, es porque este sudoku no tiene celdas libres.
		 * - en otro caso, branch.hasNext() debe ser true.
		 */
		if(branch == null) {
			/** Si no estamos done, y aun asi no hay branch ni
			 * values, quiere decir que este sudoku no tiene ningun
			 * pivote, es decir, ninguna celda libre. Por tanto,
			 * es su propia solucion.
			 */
			done = true;
			return root;
		}
		return branch.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
