package es.nextiraone.ing.sudoku.test;

import org.junit.Assert;
import org.junit.Test;

import es.nextiraone.ing.sudoku.core.DeadEndException;
import es.nextiraone.ing.sudoku.core.Sudoku;
import es.nextiraone.ing.sudoku.core.Cache;


public class SudokuFixTest extends SudokuBase {

	private int row;
	private int col;
	private int val;

	public void mySetUp() {
		row  = size / 2;
		col  = size / 3;
		val  = size / 4 + 1;
	}

	@Test
	public void testFixDoesFix() throws DeadEndException {
		/** Comprueba que Fix efectivamente fija las celdas
		 */
		sudoku.fix(new Sudoku.Fix(row, col, val));
		cell = sudoku.getAt(row, col);
		Assert.assertEquals(cell.getLength(), 1);
		Assert.assertEquals(cell.getValue(), val);
	}

	@Test
	public void testFixDoesDrop() throws DeadEndException {
		/** Comprueba que Fix elimina valores de las celdas adyacentes
		 */
		sudoku.fix(new Sudoku.Fix(row, col, val));
		// Compruebo que ha quitado el elemento de la fila
		for(int i = 0; i < size; i++) {
			if(i != col) {
				cell = sudoku.getAt(row, i);
				Assert.assertEquals(cell.getLength(), size-1);
				Assert.assertEquals(cell.contains(val), false);
			}
		}
		// Compruebo que ha quitado el elemento de la columna
		for(int i = 0; i < size; i++) {
			if(i != row) {
				cell = sudoku.getAt(i, col);
				Assert.assertEquals(cell.getLength(), size-1);
				Assert.assertEquals(cell.contains(val), false);
			}
		}
		// Compruebo que ha quitado el elemento del cuadro
		int[] translation = Cache.translate(row, col);
		int square = translation[0];
		int index  = translation[1];
		for(int i = 0; i < size; i++) {
			if(i != index) {
				translation = Cache.translate(square, i);
				row  = translation[0];
				col  = translation[1];
				cell = sudoku.getAt(row, col);
				Assert.assertEquals(cell.getLength(), size-1);
				Assert.assertEquals(cell.contains(val), false);
			}
		}	
	}
	
	@Test
	public void testFixDoesCombine() throws DeadEndException {
		/** Comprueba que fix aplica la heuristica */
		/* Voy a construir un sudoku como este:
		 * 
		 *  0 0 0 1 0 0 0 0 0
		 *  0 0 0 0 0 0 1 0 0
		 *  0 0 0 0 0 0 0 0 0
		 *  1 0 0 0 0 0 0 0 0
		 *  0 0 0 0 0 0 0 0 0
		 *  0 0 0 0 0 0 0 0 0
		 *  0 1 0 0 0 0 0 0 0
		 *  0 0 0 0 0 0 0 0 0
		 *  0 0 0 0 0 0 0 0 0
		 *  
		 *  Los "1" de la primera y segunda fila y primera y
		 *  segunda columna ya estan puestos, de manera que cruzando
		 *  se ve que el "1" del primer cuadro debe estar en la tercera
		 *  fila, tercera columna.
		 *  
		 *  A ver si el sudoku aplica la combinatoria necesaria
		 *  para darse cuenta...
		 */
		sudoku.fix(new Sudoku.Fix(0, 3, 1));
		sudoku.fix(new Sudoku.Fix(1, 6, 1));
		sudoku.fix(new Sudoku.Fix(3, 0, 1));
		sudoku.fix(new Sudoku.Fix(6, 1, 1));
		cell = sudoku.getAt(2, 2);
		Assert.assertEquals(cell.getLength(), 1);
		Assert.assertEquals(cell.getValue(),  1);
	}
}
