package es.nextiraone.ing.sudoku.test;

import org.junit.Assert;
import org.junit.Test;

import es.nextiraone.ing.sudoku.core.Cache;


public class CacheTest {

	@Test
	public void testTranslate() {
		/** Comprueba que traduce las coordenadas adecuadamente
		 * 
		 *  Copio aqui para referencia la matriz de traducciones:
		 *  
		 *       0     1     2      3     4     5      6     7     8
         *    -----------------  -----------------  -----------------
    	 * 0    0.0 | 0.1 | 0.2 |  1.0 | 1.1 | 1.2 |  2.0 | 2.1 | 2.2 |
    	 * 1    0.3 | 0.4 | 0.5 |  1.3 | 1.4 | 1.5 |  2.3 | 2.4 | 2.5 |
    	 * 2    0.6 | 0.7 | 0.8 |  1.6 | 1.7 | 1.8 |  2.6 | 2.7 | 2.9 |
         *    -----------------  -----------------  -----------------
         *    -----------------  -----------------  -----------------
    	 * 3    3.0 | 3.1 | 3.2 |  4.0 | 4.1 | 4.2 |  5.0 | 5.1 | 5.2 |
    	 * 4    3.3 | 3.4 | 3.5 |  4.3 | 4.4 | 4.5 |  5.3 | 5.4 | 5.5 |
    	 * 5    3.6 | 3.7 | 3.8 |  4.6 | 4.7 | 4.8 |  5.6 | 5.7 | 5.9 |
         *    -----------------  -----------------  -----------------
         *    -----------------  -----------------  -----------------
    	 * 6    6.0 | 6.1 | 6.2 |  7.0 | 7.1 | 7.2 |  8.0 | 8.1 | 8.2 |
    	 * 7    6.3 | 6.4 | 6.5 |  7.3 | 7.4 | 7.5 |  8.3 | 8.4 | 8.5 |
    	 * 8    6.6 | 6.7 | 6.8 |  7.6 | 7.7 | 7.8 |  8.6 | 8.7 | 8.9 |
         *    -----------------  -----------------  -----------------
		 */
		int[] array_70 = { 7, 0 };
		Assert.assertArrayEquals(Cache.translate(6, 3), array_70);
		int[] array_35 = { 3, 5 };
		Assert.assertArrayEquals(Cache.translate(4, 2), array_35);
	}

}
