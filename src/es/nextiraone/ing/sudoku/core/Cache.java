package es.nextiraone.ing.sudoku.core;


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
	 * (fila: X, columna: Y) es muy facil: posicion = (X * DIMS +  Y).
	 * 
	 * Pero para aislar al resto de clases de la necesidad de conocer estos
	 * detalles, y para facilitar y acelerar el procesamiento, las funciones
	 * relativas a offsets, bits y mascaras se encapsulan todas en esta clase.
	 */

	// lado de cada cuadro del sudoku
	public final int SIDE;
	// lado del sudoku
	public final int DIMS;
	// numero total de celdas en el sudoku
	public final int CELLS;
	// numero de valores posibles de la mascara
	// (ver funcion _buildMask)
	public final int VALS;
	// celda sin restringir (con todos los valores posbles) 
	public final int FULLCELL;
	// mascara vacia (sin opciones)
	public final int EMPTYMASK;

	private final int[][] ROW;
	private final int[][] COL;
	private final int[][] SQUARE;
	private final OffsetGroup[][] NEIGHBOR;

	public Cache(int side) {
		SIDE      = side;
		DIMS      = SIDE * SIDE;
		CELLS     = DIMS * DIMS;
		VALS      = 1 << DIMS;
		FULLCELL  = VALS - 1;
		EMPTYMASK = 0;
		ROW       = buildRow();
		COL       = buildCol();
		SQUARE    = buildSquare();
		NEIGHBOR  = buildNeighbor();
	}

	public final int[] translate(final int coord1, final int coord2) {
		/** Traduce coordenadas (fila, columna) a (cuadro, indice) y viceversa.
		 * 
		 * La traduccion es simetrica, asi que funciona en los dos sentidos:
		 * si le pasas a la funcion un par (fila, columna) te devuelve un par
		 * (cuadro, indice). Si le pasas (cuadro, indice) te devuelve (fila,
		 * columna). 
		 */
		final int aGroup  = coord1 / SIDE;
		final int aOffset = coord1 % SIDE;
		final int bGroup  = coord2 / SIDE;
		final int bOffset = coord2 % SIDE;
		final int[] result = { aGroup * SIDE + bGroup, aOffset * SIDE + bOffset };
		return result;
	}

	public final int getOffset(final int row, final int col) {
		/** Devuelve el offset de la celda dentro del array */
		return row * DIMS + col;
	}

	private final int[][] buildRow() {
		/** Precalcula el ofset de cada celda en funcion de (fila, columna) */
		int[][] out = new int[DIMS][DIMS];
		int offset  = 0;
		for(int row = 0; row < DIMS; row++) {
			for(int col = 0; col < DIMS; col++, offset++) {
				out[row][col] = offset;
			}
		}
		return out;
	}

	private final int[][] buildCol() {
		/** Precalcula el ofset de cada celda en funcion de (columna, fila) */
		int[][] out = new int[DIMS][DIMS];
		for(int col = 0; col < DIMS; col++) {
			for(int row = 0; row < DIMS; row++) {
				out[col][row] = getOffset(row, col);
			}
		}
		return out;
	}

	private final int[][] buildSquare() {
		/** Precalcula el ofset de cada celda en funcion de (cuadro, indice) */
		int[][] out = new int[DIMS][DIMS];
		for(int square = 0; square < DIMS; square++) {
			for(int index = 0; index < DIMS; index++) {
				int[] xlate = translate(square, index);
				out[square][index] = getOffset(xlate[0], xlate[1]);
			}
		}
		return out;
	}

	private final OffsetGroup[][] buildNeighbor() {
		OffsetGroup[][] out = new OffsetGroup[CELLS][3];
		for(int square = 0; square < DIMS; square++) {
			for(int index = 0; index < DIMS; index++) {
				int offset  = SQUARE[square][index];
				int rowSkip = (square % SIDE) * SIDE;
				int colSkip = (square / SIDE) * SIDE;
				int[] xlate = translate(square, index);
				out[offset][0] = new OffsetGroup(this, SQUARE[square], index, index + 1);
				out[offset][1] = new OffsetGroup(this, ROW[xlate[0]], rowSkip, rowSkip + SIDE);
				out[offset][2] = new OffsetGroup(this, COL[xlate[1]], colSkip, colSkip + SIDE);
			}
		}
		return out;
	}

	protected final int[] getRow(final int row) {
		/** Devuelve las coordenadas de las celdas en la fila dada */
		return ROW[row];
	}

	protected final int[][] getRows() {
		/** Devuelve las coordenadas de todas las filas */
		return ROW;
	}

	protected final int[] getCol(final int col) {
		/** Devuelve las coordenadas de las celdas en la columna dada */
		return COL[col];
	}

	protected final int[][] getCols() {
		/** Devuelve las coordenadas de todas las columnas */
		return COL;
	}

	protected final int[] getSquare(final int square) {
		/** Devuelve las coordenadas de las celdas en el cuadro dado */
		return SQUARE[square];
	}

	protected final int[][] getSquares() {
		/** Devuelve las coordenadas de todos los cuadros */
		return SQUARE;
	}

	protected final int getMask(final int index) {
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
		return (1 << index);
	}

	protected final int getMaskUpto(final int index) {
		/** Devuelve una mascara que contiene todos los valores desde 0 hasta value
		 * 
		 * 0 es inclusivo, value no. 
		 */
		return (1 << index) - 1;
	}

	protected final int getMaskWithout(final int mask, final int index) {
		/** Quita de la mascara el bit en el indice dado por value */
		return mask & (~(1 << index));
	}

	protected final int getCellWithout(final int cell, final int options) {
		/** Elimina los bits indicados por options de la celda */
		return cell & ~options;
	}

	protected final int getCellCombined(final int cell, final int options) {
		/** Elimina los bits indicados por options de la celda */
		return cell | options;
	}

	protected final boolean doesCellContain(final int cell, final int options) {
		/** Comprueba si la celda contiene los bits dados */
		return ((cell & options) == options);
	}

	protected final int getLength(final int cell) {
		/** Devuelve el numero de opciones de la celda */
		return Integer.bitCount(cell);
	}
	
	protected final OffsetGroup[] getNeighbor(final int offset) {
		/** Devuelve las coordenadas de las celdas vecinas a la dada */
		return NEIGHBOR[offset];
	}

	protected final OptionIterator getOption(final int cell) {
		/**
		 * Devuelve los identificadores de los diferentes valores que
		 * puede tomar la celda.
		 * 
		 * Los indices empiezan a contar desde 0, es decir:
		 * 
		 * - Si el bit que representa el valor 1 esta encendido, devuelve "0"
		 * - Si el bit que representa el valor 2 esta encendido, devuelve "1"
		 * - Si el bit que representa el valor 3 esta encendido, devuelve "2"
		 * ... etc ...
		 */
		return new OptionIterator(this, cell);
	}

	protected final OptionIterator getOption(final int cell, final int offset) {
		/**
		 * Devuelve los identificadores de los diferentes valores que
		 * puede tomar la celda.
		 * 
		 * Los indices empiezan a contar desde "offset", es decir:
		 * 
		 * - Si el bit que representa el valor 1 esta encendido, devuelve "offset+0"
		 * - Si el bit que representa el valor 2 esta encendido, devuelve "offset+1"
		 * - Si el bit que representa el valor 3 esta encendido, devuelve "offset+2"
		 * ... etc ...
		 */
		return new OptionIterator(this, cell, offset);
	}

	protected final int getValue(final int cell) {
		/** Devuelve el valor que corresponde a la celda.
		 * 
		 * Solo se debe invocar si la celda tiene un unico
		 * valor (Cache.getLength(cell) == 1). Si no, el valor
		 * devuelto sera incorrecto.
		 */
		return Integer.numberOfTrailingZeros(cell) + 1;
	}
}
