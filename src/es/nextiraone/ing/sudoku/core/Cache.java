package es.nextiraone.ing.sudoku.core;


public final class Cache {

	/*
    Cache de las diversas tablas que se necesitan para procesar sudokus.

    El origen de todas las tablas es una matriz que permite traducir
    coordenas expresadas como (fila, columna) a coordenadas expresadas
    como (cuadro, indice), y viceversa.

    En la representacion de abajo, estan numeradas las filas y columnas
    del sudoku, y dentro de cada celda, esta el numero de cuadro y de
    indice dentro del cuadro de la celda correspondiente:

          0     1     2      3     4     5      6     7     8
         -----------------  -----------------  -----------------
    0    0.0 | 0.1 | 0.2 |  1.0 | 1.1 | 1.2 |  2.0 | 2.1 | 2.2 |
    1    0.3 | 0.4 | 0.5 |  1.3 | 1.4 | 1.5 |  2.3 | 2.4 | 2.5 |
    2    0.6 | 0.7 | 0.8 |  1.6 | 1.7 | 1.8 |  2.6 | 2.7 | 2.9 |
         -----------------  -----------------  -----------------
         -----------------  -----------------  -----------------
    3    3.0 | 3.1 | 3.2 |  4.0 | 4.1 | 4.2 |  5.0 | 5.1 | 5.2 |
    4    3.3 | 3.4 | 3.5 |  4.3 | 4.4 | 4.5 |  5.3 | 5.4 | 5.5 |
    5    3.6 | 3.7 | 3.8 |  4.6 | 4.7 | 4.8 |  5.6 | 5.7 | 5.9 |
         -----------------  -----------------  -----------------
         -----------------  -----------------  -----------------
    6    6.0 | 6.1 | 6.2 |  7.0 | 7.1 | 7.2 |  8.0 | 8.1 | 8.2 |
    7    6.3 | 6.4 | 6.5 |  7.3 | 7.4 | 7.5 |  8.3 | 8.4 | 8.5 |
    8    6.6 | 6.7 | 6.8 |  7.6 | 7.7 | 7.8 |  8.6 | 8.7 | 8.9 |
         -----------------  -----------------  -----------------

    Si linealizamos el sudoku en una tupla por cuadros (es decir,
    poniendo los elementos del primer cuadro como los nueve primeros
    elementos de la tupla, los del segundo cuadro como los nueve
    siguientes de la tupla, etc), tenemos la siguiente correspondencia
    entre cada celda y su posicion en el array:

          0     1     2      3     4     5      6     7     8
         -----------------  -----------------  -----------------
    0     0  |  1  |  2  |   9  | 10  | 11  |  18  | 19  | 20  |
    1     3  |  4  |  5  |  12  | 13  | 14  |  21  | 22  | 23  |
    2     6  |  7  |  8  |  15  | 16  | 17  |  24  | 25  | 26  |
         -----------------  -----------------  -----------------
         -----------------  -----------------  -----------------
    3    27  | 28  | 29  |  36  | 37  | 38  |  45  | 46  | 47  |
    4    30  | 31  | 32  |  39  | 40  | 41  |  48  | 49  | 50  |
    5    33  | 34  | 35  |  42  | 43  | 44  |  51  | 52  | 53  |
         -----------------  -----------------  -----------------
         -----------------  -----------------  -----------------
    6    54  | 55  | 56  |  63  | 64  | 65  |  72  | 73  | 74  |
    7    57  | 58  | 59  |  66  | 67  | 68  |  75  | 76  | 77  |
    8    60  | 61  | 62  |  69  | 70  | 71  |  78  | 79  | 80  |
         -----------------  -----------------  -----------------

    Con esta tabla, puedo calcular en que posicion dentro de la tupla
    esta una celda en las coordenadas (fila, columna) dadas. A partir
    de ella, calculo todas las caches necesarias:

    * row[X]:  Coordenadas de todas las celdas de la fila X.
    * col[X]:  Coordenadas de todas las celdas de la columna X.
    * sq[X]:   Coordenadas de todas las celdas de del cuadro X.

    También tengo otras caches de coordenadas utiles:

    * neighbor[I]:  Coordenadas de todas las celdas vecinas de una dada
                    (misma fila, columna o cuadro)
    * pos[I]:       tripleta (cuadro, fila, columna) correspondiente a
                    cada posicion de la tupla (ejemplo: el elemento 12
                    de la tupla es la celda en el cuadro 1, fila 1,
                    columna 3 (contando desde 0).

    Caches para valores particulares de las celdas:

    * length[C]:    numero de bits a "1"s en la celda.
    * value[C]:     si length[x] == 1, value[x] es el digito en la celda.
                    en otro caso, value[x] == 0.

    Y algunas mascaras de bits:

    * ALL:      celda con todos los bits a 1.
    * mask[X]:  mascara de bits de cada valor posible 1..9

    En estos arrays:

    * X va de 0 a (9)-1.
    * I va de 0 a (9*9)-1.
    * C va de 0 a (2**9)-1.
    */

	public static final int SIDE  = 3;
	public static final int DIMS  = SIDE*SIDE;
	public static final int CELLS = DIMS*DIMS;
	public static final int VALS  = 1 << DIMS;

	private static final int[] _buildIndex() {
		/// Construye la tabla de traduccion (fila, columna) => celda
		/* Construyo un array de la forma:
		 * {
		 * 	{ 0, 1, 2 },
		 *  { 3, 4, 5 },
		 *  { 6, 7, 8 }
		 *  }
		 */
		int[][] set = new int[SIDE][SIDE];
		for(int i = 0; i < SIDE; i++) {
			for(int j = 0; j < SIDE; j++) {
				set[i][j] = i*SIDE + j;
			}
		}
		int idx = 0;
		/* Y ahora construyo la lista de offsets: dada
		 * una celda por sus coordenadas (fila, columna),
		 * su posicion en el array es
		 * out[fila * DIMS + columna]
		 */
		int[] out = new int[CELLS];
		for(int[] xset: set) {
			for(int[] yset: set) {
				for(int x: xset) {
					for(int y: yset) {
						out[idx] = x*DIMS+y;
						idx += 1;
					}
				}
			}
		}
		return out;
	}

	private static final int[] _buildMask() {
		/// Construye las mascaras de bits de cada digito
		/* Para cada valor "n" entre 1 y DIMS, la mascara que le va
		 * a corresponder es 1 << (n-1). 
		 */
		int[] out = new int[DIMS];
		for(int i = 0; i < DIMS; i++) {
			out[i] = (1 << i);
		}
		return out;
	}

	private static final int[] _buildLength() {
		/// Calcula el numero de bits a "1" en cada celda
		int[] out = new int[VALS];
		for(int i = 0; i < VALS; i++) {
			out[i] = Integer.bitCount(i);
		}
		return out;
	}

	private static final int[] _buildValue() {
		/// Si la celda representa un valor unico, calcula ese valor. Si no, 0.
		int[] out = new int[VALS];
		for(int i = 0; i < VALS; i++) {
			out[i] = 0;
			if(LENGTH[i] == 1) {
				for(int j = 0; j < DIMS; j++) {
					if(MASK[j] == i) {
						out[i] = j+1;
					}
				}
			}
		}
		return out;
	}

	private static final int[][] _buildRow() {
		// Lista los indices de las celdas de cada fila
		int[][] out = new int[DIMS][DIMS];
		for(int row = 0; row < DIMS; row++) {
			for(int col = 0; col < DIMS; col++) {
				out[row][col] = INDEX[row*DIMS+col];
			}
		}
		return out;
	}

	private static final int[][] _buildCol() {
		// Lista los indices de las celdas de cada columna
		int[][] out = new int[DIMS][DIMS];
		for(int col = 0; col < DIMS; col++) {
			for(int row = 0; row < DIMS; row++) {
				out[col][row] = INDEX[row*DIMS+col];
			}
		}
		return out;
	}

	private static final int[][] _buildSq() {
		// Lista los indices de las celdas de cada cuadro
		int[][] out = new int[DIMS][DIMS];
		for(int sq = 0; sq < DIMS; sq++) {
			for(int idx = 0; idx < DIMS; idx++) {
				out[sq][idx] = (sq*DIMS+idx);
			}
		}
		return out;
	}

	private static final int[][] _buildPos() {
		// Devuelve la tripleta (cuadro, fila, columna) de cada celda
		int[][] out = new int[CELLS][3];
		for(int i = 0; i < CELLS; i++) {
			out[i][0] = i / DIMS;
			out[i][1] = INDEX[i] / DIMS;
			out[i][2] = INDEX[i] % DIMS;
		}
		return out;
	}

	private static final int[][][] _buildNeighbor() {
		// Lista las celdas vecinas (mismo cuadro, fila o columna)
		// por cada celda, devuelve tres arrays: uno con los
		// indices de las celdas del mismo cuadro, otro con las
		// de la misma fila, y otro con los de la misma columna.
		int[][][] out = new int[CELLS][3][DIMS-1];
		for(int i = 0; i < CELLS; i++) {
			int[] pos      = POS[i];
			int[][] coords = { SQ[pos[0]], ROW[pos[1]], COL[pos[2]] };
			int axis       = 0;
			for(axis = 0; axis < coords.length; axis++) {
			    int idx = 0;
			    for(int x = 0; x < DIMS; x++) {
			    	int coord = coords[axis][x];
				    if(coord != i) {
					    out[i][axis][idx] = coord;
					    idx += 1;
				    }
				}
			}
		}
		return out;
	}

	private static final int[][] _buildOpt() {
		/// Lista los indices de todos los bits a "1" de cada valor.
		int[][] out = new int[VALS][];
		for(int i = 0; i < VALS; i++) {
			out[i]  = new int[LENGTH[i]];
			int idx = 0;
			for(int j = 0; j < MASK.length; j++) {
				if((i & MASK[j]) != 0) {
					out[i][idx] = j;
					idx += 1;
				}
			}
		}
		return out;
	}

	protected static final int ALL            = VALS-1;
	protected static final int[] INDEX        = _buildIndex();
	protected static final int[] MASK         = _buildMask();
	protected static final int[] LENGTH       = _buildLength();
	protected static final int[] VALUE        = _buildValue();
	protected static final int[][] ROW        = _buildRow();
	protected static final int[][] COL        = _buildCol();
	protected static final int[][] SQ         = _buildSq();
	protected static final int[][] POS        = _buildPos();
	protected static final int[][][] NEIGHBOR = _buildNeighbor();
	protected static final int[][] OPT        = _buildOpt();
	
	public static final int[] translate(int coord1, int coord2) {
		/** Traduce coordenadas (fila, columna) a (cuadro, indice) y viceversa.
		 * 
		 * La traduccion es simetrica, asi que funciona en los dos sentidos:
		 * si le pasas a la funcion un par (fila, columna) te devuelve un par
		 * (cuadro, indice). Si le pasas (cuadro, indice) te devuelve (fila,
		 * columna). 
		 */
		int   offset = INDEX[coord1*DIMS + coord2];
		int[] result = { offset/DIMS, offset%DIMS };
		return result;
	}
}
