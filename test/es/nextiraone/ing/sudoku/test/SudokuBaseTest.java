package es.nextiraone.ing.sudoku.test;

import org.junit.Assert;
import org.junit.Test;


public class SudokuBaseTest extends SudokuBase {

	@Test
	public void testSudoku() {
		/** Compruebo que el sudoku se construye correctamente.
		 * 
		 * Basicamente, me aseguro de que todas las celdas empiezan
		 * vacias, con todas las posibilidades.
		 */
		int[] all = new int[size];
		for(int idx = 0; idx < size; idx++) {
			all[idx] = idx + 1;
		}
		for(int row = 0; row < size; row++) {
			for(int col = 0; col < size; col++) {
				cell  = sudoku.getAt(row, col);
				Assert.assertEquals(cell.getLength(), size);
				int[] values = new int[cell.getLength()];
				int index    = 0;
				for(int value: cell.getValues()) {
					values[index++] = value;
				}
				Assert.assertArrayEquals(values, all);
			}
		}
	}
}
