package es.nextiraone.ing.sudoku.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public final class Sorter implements Comparator<Integer> {

    /** Comparador para ordenar las celdas del sudoku.
     * 
     * Ordena las celdas en funcion de la cantidad de opciones
     * que le quedan a cada celda, de menos a mas.
     */

	private final int[] cells;
		
	public Sorter(Sudoku root) {
		this.cells = root.getCells();
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		final int len1 = Cache.getLength(cells[o1]);
		final int len2 = Cache.getLength(cells[o2]);
		if(len1 > len2) return  1;
		if(len1 < len2) return -1;
		return 0;
	}
	
	public List<Integer> free() {
		/** Devuelve una lista de indices de celdas cuyo valor no esta fijado
         * 
         * La lista primero se randomiza, para asegurar que el orden de las
         * celdas del mismo tamaño es aleatorio. Luego, se ordena de menos a
         * mas opciones de celda.
         */
		/* La lista esta ordenada por numero de opciones que le
		 * quedan a la celda, de menor a mayor.
		 */
		List<Integer> free = new ArrayList<Integer>();
   		for(int i = 0; i < Cache.CELLS; i++) {
   			if(Cache.getLength(cells[i]) > 1)
   				free.add(new Integer(i));
   		}
		/* Randomizo los indices, para que el orden de los
		 * elementos en el array no sea lineal y las soluciones
		 * a un sudoku no sean siempre las mismas.
		 */
   		Collections.shuffle(free);
   		Collections.sort(free, this);
   		return free;
	}
}

