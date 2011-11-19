package es.nextiraone.ing.sudoku.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class Cache {

	/**
     * Cache de las diversas tablas que se necesitan para procesar sudokus.
	 * 
     * En la representacion de abajo, estan numeradas las filas y columnas
     * del sudoku, y dentro de cada celda, esta el numero de cuadro y de
     * indice dentro del cuadro de la celda correspondiente:
	 * 
     *       0     1     2      3     4     5      6     7     8
     *     -----------------  -----------------  -----------------
     * 0    0.0 | 0.1 | 0.2 |  1.0 | 1.1 | 1.2 |  2.0 | 2.1 | 2.2 |
     * 1    0.3 | 0.4 | 0.5 |  1.3 | 1.4 | 1.5 |  2.3 | 2.4 | 2.5 |
     * 2    0.6 | 0.7 | 0.8 |  1.6 | 1.7 | 1.8 |  2.6 | 2.7 | 2.9 |
     *     -----------------  -----------------  -----------------
     *     -----------------  -----------------  -----------------
     * 3    3.0 | 3.1 | 3.2 |  4.0 | 4.1 | 4.2 |  5.0 | 5.1 | 5.2 |
     * 4    3.3 | 3.4 | 3.5 |  4.3 | 4.4 | 4.5 |  5.3 | 5.4 | 5.5 |
     * 5    3.6 | 3.7 | 3.8 |  4.6 | 4.7 | 4.8 |  5.6 | 5.7 | 5.9 |
     *     -----------------  -----------------  -----------------
     *     -----------------  -----------------  -----------------
     * 6    6.0 | 6.1 | 6.2 |  7.0 | 7.1 | 7.2 |  8.0 | 8.1 | 8.2 |
     * 7    6.3 | 6.4 | 6.5 |  7.3 | 7.4 | 7.5 |  8.3 | 8.4 | 8.5 |
     * 8    6.6 | 6.7 | 6.8 |  7.6 | 7.7 | 7.8 |  8.6 | 8.7 | 8.9 |
     *     -----------------  -----------------  -----------------
	 * 
	 * Una curiosidad es que la correspondencia entre
	 * (fila, columna) y (cuadro, indice) es simetrica, por ejemplo:
	 * 
	 * - En la (fila   3, columna 4) esta el     (cuadro 4, indice  1)
	 * - En el (cuadro 3, indice  4) esta en la  (fila   4, columna 1)
	 * 
	 * Es decir, si (fila X, columna Y) -> (cuadro A, indice B), entonces
	 * (fila A, columna B) -> (cuadro X, indice Y).
	 * 
	 * Curiosidades aparte, el sudoku lo representaremos como una tabla
	 * lineal de DIMS * DIMS enteros, y querremos conocer el indice dentro
	 * de esa tabla de la celda en la posicion (fila, columna), o de la
	 * celda en la posicion (cuadro, indice).
	 * 
	 * Por convencion, vamos a hacer que la representacion lineal del sudoku
	 * sea por filas, asi que conocer la posicion de la celda
	 * (fila: X, columna: Y) es muy facil: posicion = (X * DIMS +  Y)
	 * 
	 *  Sin embargo, conocer la posicion de la celda (cuadro: X, indice: Y)
	 *  requiere hacer otros calculos algo menos intuitivos.
	 *  
	 *  Esta clase sirve como contenedor para diversas caches y funciones que
	 *  facilitan y aceleran este y otros calculos similares, y sobre todo
	 *  permiten reutilizar objetos y no tener que andar haciendo new() en
	 *  la mayoria de los casos.
	 */

	// lado de cada cuadro del sudoku
	public static final int SIDE = 3;
	// lado del sudoku
	public static final int DIMS = SIDE*SIDE;
	// numero total de celdas en el sudoku
	public static final int CELLS = DIMS*DIMS;
	// numero de valores posibles de la mascara
	// (ver funcion _buildMask)
	public static final int VALS = 1 << DIMS;
	// celda vacia (sin restringir) 
	public static final int EMPTYCELL = VALS-1;

	private static final int[][] ROW = _buildRow();
	private static final int[][] _buildRow() {
		/** Precalcula el ofset de cada celda en funcion de (fila, columna) */
		int[][] out = new int[DIMS][DIMS];
		for(int row = 0; row < DIMS; row++) {
			for(int col = 0; col < DIMS; col++) {
				out[row][col] = row*DIMS + col;
			}
		}
		return out;
	}

	private static final int[][] COL = _buildCol();
	private static final int[][] _buildCol() {
		/** Precalcula el ofset de cada celda en funcion de (columna, fila) */
		int[][] out = new int[DIMS][DIMS];
		for(int col = 0; col < DIMS; col++) {
			for(int row = 0; row < DIMS; row++) {
				out[col][row] = row*DIMS + col;
			}
		}
		return out;
	}

	private static final int[][] SQUARE = _buildSquare();
	private static final int[][] _buildSquare() {
		/** Precalcula el ofset de cada celda en funcion de (cuadro, indice) */
		int[][] out = new int[DIMS][DIMS];
		int square  = 0;
		for(int sqGroup = 0; sqGroup < SIDE; sqGroup++) { 
			for(int sqOffset = 0; sqOffset < SIDE; sqOffset++, square++) {
				int index = 0;
				for(int idxGroup = 0; idxGroup < SIDE; idxGroup++) {
					for(int idxOffset = 0; idxOffset < SIDE; idxOffset++, index++) {
						int row = sqGroup*SIDE  + idxGroup;
						int col = sqOffset*SIDE + idxOffset;
						out[square][index] = row*DIMS + col;
					}
				}
			}
		}
		return out;
	}

	public static final class Coords {
		
		/** Coordenada de una celda dentro del sudoku */

		private final int row;
		private final int col;
		private final int square;
		private final int index;
		
		public Coords(final int row, final int col, final int square, final int index) {
			this.row    = row;
			this.col    = col;
			this.square = square;
			this.index  = index;
		}
		
		public final int getRow()    { return row; }
		public final int getCol()    { return col; }
		public final int getSquare() { return square; }
		public final int getIndex()  { return index; }
	}

	private static final Coords[] COORDS = _buildCoords();
	private static final Coords[] _buildCoords() {
		/** Precalcula las coordenadas de cada posible offset.
		 * 
		 * Invierte la relacion (fila, columna) -> (offset en array)
		 * o (cuadro, indice) -> (offset en array). Precalcula una
		 * tabla con la fila, columna, cuadro e indice de cada posible
		 * offset en el sudoku linealizado.
		 */
		Coords[] out = new Coords[CELLS];
		for(int idx = 0; idx < CELLS; idx++) {
			int row    = idx / DIMS;
			int col    = idx % DIMS;
			int translated = SQUARE[row][col];
			int square = translated / DIMS;
			int index  = translated % DIMS;
			out[idx] = new Coords(row, col, square, index);
		}
		return out;
	}

	private static final int[] copy(int[] from, int start1, int end1, int start2, int end2) {
		/** Copia dos trozos de un array en otro nuevo.
		 *
		 * Para los dos trozos, start es inclusivo y end no.
		 */
		int[] to = new int[(end1-start1)+(end2-start2)];
		int base = 0;
		for(; start1 < end1; start1++) {
			to[base++] = from[start1];
		}
		for(; start2 < end2; start2++) {
			to[base++] = from[start2];
		}
		return to;
	}

	private static final int[][][] NEIGHBOR = _buildNeighbor();
	private static final int[][][] _buildNeighbor() {
		/** Lista las celdas vecinas (mismo cuadro, fila o columna)
		 * 
		 * Guia para acceder al array devuelto:
		 * 
		 * - El primer indice se corresponde con el offset de la celda
		 *   cuyos vecinos quieres encontrar
		 * - El segundo indice es 0 para acceder a las vecinas del mismo
		 *   cuadro, 1 para la misma fila, y 2 para la misma columna.
		 *
		 * Asi, por ejemplo,
		 * NEIGHBOR[34][0] => Celdas en el mismo cuadro que la celda 34.
		 * NEIGHBOR[12][1] => Celdas en la misma fila que la celda 12.
		 */
		int[][][] out = new int[CELLS][3][];
		for(int offset = 0; offset < CELLS; offset++) {
			Coords pos = COORDS[offset];
			// En el mismo cuadro: todas las de su cuadro menos ella
			out[offset][0] = copy(SQUARE[pos.square], 0, pos.index, pos.index+1, DIMS);
			// En la misma fila: todas menos las de su cuadro
			int colGroup = pos.square % SIDE;
			out[offset][1] = copy(ROW[pos.row], 0, colGroup*SIDE, (colGroup+1)*SIDE, DIMS);
			// En la misma columna: todas menos las de su cuadro
			int rowGroup = pos.square / SIDE;
			out[offset][2] = copy(COL[pos.col], 0, rowGroup*SIDE, (rowGroup+1)*SIDE, DIMS);
		}
		return out;
	}

	private static final Map<Integer, Integer> VALUE = _buildValue();
	private static final java.util.Map<Integer, Integer> _buildValue() {
		/** Construye un mapa inverso de MASK */		
		HashMap<Integer, Integer> out = new HashMap<Integer, Integer>();
		for(int index = 0; index < DIMS; index++) {
			int mask = Cache.getMask(index);
			out.put(mask, index + 1);
		}
		return out;
	}

	protected static final int[] getRow(final int row) {
		/** Devuelve las coordenadas de las celdas en la fila dada */
		return ROW[row];
	}

	protected static final int[][] getRows() {
		/** Devuelve las coordenadas de todas las filas */
		return ROW;
	}

	protected static final int[] getCol(final int col) {
		/** Devuelve las coordenadas de las celdas en la columna dada */
		return COL[col];
	}

	protected static final int[][] getCols() {
		/** Devuelve las coordenadas de todas las columnas */
		return COL;
	}

	protected static final int[] getSquare(final int square) {
		/** Devuelve las coordenadas de las celdas en el cuadro dado */
		return SQUARE[square];
	}

	protected static final int[][] getSquares() {
		/** Devuelve las coordenadas de todos los cuadros */
		return SQUARE;
	}

	protected static final int getMask(final int value) {
		/** Devuelve la mascara de bits de un valor.
		 *
		 * Cada celda del sudoku puede tomar un valor dentro de
		 * una lista de valores posibles, de "1" a "DIMS". El
		 * numero de valores que puede tomar una celda se va
		 * reduciendo en funcion del valor de las otras celdas
		 * de la misma fila, columna o cuadro.
		 * 
		 * Para llevar el control de los valores que todavia son
		 * validos para una celda, usamos una mascara de bits. Cada
		 * digito de "1" a "DIMS" esta representado por un bit, que
		 * estara a "1" si la celda todavia puede tomar ese valor,
		 * o a "0" si no puede. Usando este mecanismo, una celda
		 * puede representarse por un numero entero de "DIMS" bits. 
		 * 
		 * Esta funcion devuelve la mascara de bits de un valor,
		 * Pero empezando a contar desde 0. Es decir, si quieres la
		 * mascara del numero "1", usaras getMask(0).
		 */
		return (1 << value);
	}

	protected static final int getLength(final int cell) {
		/** Devuelve el numero de opciones de la celda */
		return Integer.bitCount(cell);
	}
	
	protected static final Coords getCoords(final int offset) {
		/** Devuelve las coordenadas correspondientes al offset dado */
		return COORDS[offset];
	}
	
	protected static final int[][] getNeighbor(final int offset) {
		/** Devuelve las coordenadas de las celdas vecinas a la dada */
		return NEIGHBOR[offset];
	}
	
	public static final class OptionIterator implements Iterable<Integer>, Iterator<Integer> {
		
		/** Iterador sobre los posibles valores de una celda
		 * 
		 * Todo este cirio lo monto por no tener que hacer un new int[] cada
		 * vez que alguien utiliza getOption... no se que sera peor.
		 */

		private int cell;

		// Cacheo los numeros enteros. Son invariantes y no quiero
		// tener que ir creando objetos para nada.
		private static final Integer[] INTEGERS = _buildIntegers();
		private static final Integer[] _buildIntegers() {
			Integer[] out = new Integer[Cache.DIMS];
			for(int idx = 0; idx < Cache.DIMS; idx++) {
				out[idx] = new Integer(idx);
			}
			return out;
		}

		public OptionIterator(int cell) {
			this.cell = cell;
		}

		@Override
		public boolean hasNext() {
			return (cell != 0);
		}

		@Override
		public Integer next() {
			int index = Integer.numberOfTrailingZeros(cell);
			cell &= ~(1 << index);
			return INTEGERS[index];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<Integer> iterator() {
			return this;
		}
	}

	protected static final OptionIterator getOption(final int cell) {
		/** Devuelve los indices de los bits a 1 en la celda */
		return new OptionIterator(cell);
	}

	protected static final int getValue(final int mask) {
		/** Devuelve el valor que corresponde a la mascara (contando desde 0)
		 * 
		 * Solo se debe invocar si getLength(mask) == 1. Si
		 * no, se producira un error.
		 */
		return VALUE.get(mask);
	}

	public static final int[] translate(final int coord1, final int coord2) {
		/** Traduce coordenadas (fila, columna) a (cuadro, indice) y viceversa.
		 * 
		 * La traduccion es simetrica, asi que funciona en los dos sentidos:
		 * si le pasas a la funcion un par (fila, columna) te devuelve un par
		 * (cuadro, indice). Si le pasas (cuadro, indice) te devuelve (fila,
		 * columna). 
		 */
		final int   offset = SQUARE[coord1][coord2];
		final int[] result = { offset / DIMS, offset % DIMS };
		return result;
	}

	public static final int getOffset(int row, int col) {
		/** Traduce coordenadas (fila, columna) a offset en array */
		return row * DIMS + col;
	}
}
