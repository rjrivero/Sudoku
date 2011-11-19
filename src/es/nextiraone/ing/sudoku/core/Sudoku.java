package es.nextiraone.ing.sudoku.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Sudoku {

	/** Objeto Sudoku! */

	private int[] cells;

	public Sudoku() {
		/** Nuevo sudoku vacio */
		cells = new int[Cache.CELLS];
		for (int i = 0; i < Cache.CELLS; i++) {
			cells[i] = Cache.EMPTYCELL;
		}
	}

	protected Sudoku(final Sudoku root) {
		/** Copia de un sudoku existente */
		this.cells = Arrays.copyOf(root.cells, root.cells.length);
	}

	protected int[] getCells() {
		/** Devuelve la lista de celdas */
		return cells;
	}

	public final int getSize() {
		/** Devuelve el tamaño de lado del sudoku */
		return Cache.DIMS;
	}

	protected int getAt(int offset) {
		/** Devuelve la celda en el offset dado */
		return cells[offset];
	}

	public Cell getAt(int row, int col) {
		/** Devuelve la celda en la fila y columna dadas */
		return new Cell(cells[Cache.getRow(row)[col]]);
	}

	private boolean drop(int[] offsets, int used, int mask)
			throws DeadEndException {
		/**
		 * Ejecuta la propagacion de cambios en el sudoku
		 * 
		 * Elimina el valor como posible de las celdas especificadas, y recurre
		 * si alguna celda se ha reducido a un solo valor.
		 * 
		 * - offsets es una lista de coordenadas, de longitud <= this.getSize().
		 * - used es una bitmask, indicando las posiciones a procesar dentro del
		 *   array offsets.
		 * - mask es una bitmask con los bits a quitar de las celdas.
		 */
		boolean changed = false;
		for(int index: Cache.getOption(used)) {
			int offset = offsets[index];
			if ((cells[offset] & mask) != 0) {
				int update = cells[offset] & ~mask;
				if (update == 0) {
					/* Si llegamos a una solucion incompatible */
					throw new DeadEndException();
				}
				cells[offset] = update;
				changed = true;
				if (Cache.getLength(update) == 1) {
					/*
					 * la celda ha quedado fijada, quitamos el valor de los
					 * vecinos.
					 */
					for (int[] set : Cache.getNeighbor(offset)) {
						/*
						 * Me interesa que se revisen todas las celdas vecinas
						 * de esta. Para indicarlo, tengo que construir una
						 * mascara de bits co bits a "1" en todas las posiciones
						 * del array.
						 * 
						 * La forma mas facil de construir esa mascara es
						 * calcular pow(2, set.length) - 1, o lo que es lo
						 * mismo, (1 << set.length) - 1.
						 */
						drop(set, (1 << set.length) - 1, update);
					}
				}
			}
		}
		return changed;
	}

	private boolean combineLogic(int[] coords, int used, int check)
			throws DeadEndException {
		/**
		 * Fija valores por combinacion.
		 * 
		 * Encuentra relaciones entre celdas de un mismo grupo que permitan
		 * limitar el numero de alternativas. Por ejemplo, si dos celdas de una
		 * misma fila solo pueden tener dos valores dados, entonces esos valores
		 * deben estar en esas celdas y se pueden eliminar del resto de celdas
		 * del grupo.
		 * 
		 * - coords es una lista de Cache.DIMS coordenadas o menos.
		 * - used tiene un bit a "1" en cada coordenada de una celda no
		 *   fija.
		 * - check tiene los bits a "1" en las coordenadas que estamos
		 *   validando en este pase.
		 */
		int checklen = Cache.getLength(check);
		if (checklen >= 2) {
			if (check != used) {
				/* Proceso la combinacion actual, sumando el numero de valores
				 * posibles que pueden tomar estas celdas (es decir, los bits
				 * que tienen a 1)
				 */
				int comb = 0;
				for(int index: Cache.getOption(check)) {
					comb |= cells[coords[index]];
				}
				/*
				 * Y ahora compruebo si el numero de valores posibles es mayor o
				 * menor que el numero de celdas que he combinado. Si es menor o
				 * igual, quiere decir que esos numeros tienen que estar en esas
				 * celdas y no pueden estar en el resto.
				 */
				if (Cache.getLength(comb) <= checklen) {
					if (drop(coords, used & ~check, comb))
						return true;
				}
			}
			/*
			 * Ahora voy a repetir el proceso con las combinaciones de celdas
			 * con un elemento menos. Por ejemplo, si en este paso he buscado
			 * combinaciones de 4 celdas con 4 valores, cuando termino busco
			 * combinaciones de 3 celdas con 3 valores.
			 * 
			 * Lo primero que hago es eliminar las celdas que no interesan. Por
			 * ejemplo, si voy a buscar combinaciones de 3 celdas, no tiene
			 * sentido que meta celdas con 4 opciones.
			 */
			int oldcheck = check;
			int newlen = checklen - 1;
			int oldlen = 0;
			do {
				for(int index: Cache.getOption(check)) {
					// Si la celda tiene mas de <newlen> opciones:
					if (Cache.getLength(cells[coords[index]]) > newlen) {
						// entonces, la saco de la lista.
						check = check & ~(1 << index);
					}
				}
				// y actualizo newlen
				oldlen = newlen;
				newlen = Cache.getLength(check);
			} while (oldlen != newlen && newlen > 1);
			if (oldcheck != check) {
				/*
				 * Si he reducido la lista de opciones, la tengo que procesar
				 * para ver si la nueva lista reducida da algun resultado
				 * positivo
				 */
				return combineLogic(coords, used, check);
			}
			/*
			 * Si no he podido reducir ninguna celda, pues nada, vamos probando
			 * con subgrupos de un elemento menos que el actual.
			 */
			for(int index: Cache.getOption(check)) {
				if (combineLogic(coords, used, check & ~(1 << index)))
					return true;
			}
		}
		return false;
	}

	private boolean heuristicOnGroup(int[][] group) throws DeadEndException {
		/** Analiza estadisticamente un grupo de filas, columnas o cuadros */
		for (int[] coords : group) {
			int used = 0;
			for (int idx = 0; idx < coords.length; idx++) {
				if (Cache.getLength(cells[coords[idx]]) > 1)
					used |= Cache.getMask(idx);
			}
			if (combineLogic(coords, used, used))
				return true;
		}
		return false;
	}

	public void heuristic() throws DeadEndException {
		/** Analiza estadisticamente el sudoku */
		int[][][] groups = { Cache.getRows(), Cache.getCols(),
				Cache.getSquares() };
		boolean done = false;
		do {
			done = true;
			for (int[][] group : groups) {
				if (heuristicOnGroup(group)) {
					done = false;
					break;
				}
			}
		} while (!done);
	}

	protected void fix(int offset, int value) throws DeadEndException {
		/** Fija una celda a un valor dado, y propaga cambios */
		int mask = Cache.getMask(value - 1);
		if ((cells[offset] & mask) == 0) {
			/*
			 * No se puede fijar este valor en la celda porque no esta dentro de
			 * las opciones
			 */
			throw new DeadEndException();
		}
		/* fijamos la celda */
		cells[offset] = mask;
		/* y propagamos al resto de celdas */
		for (int[] coords : Cache.getNeighbor(offset)) {
			drop(coords, (1 << coords.length) - 1, mask);
		}
	}

	public static final class Fix {

		private int offset;
		private int value;

		public Fix(int offset, int value) {
			/** Fija la celda en el offset dado al valor especificado */
			assert (offset >= 0 && offset < Cache.CELLS);
			assert (value >= 1 && value <= Cache.DIMS);
			this.offset = offset;
			this.value = value;
		}

		public Fix(int row, int col, int value) {
			/** Fija la celda en las coordenadas dadas al valor especificado */
			assert (row >= 0 && row < Cache.DIMS);
			assert (col >= 0 && col < Cache.DIMS);
			assert (value >= 1 && value <= Cache.DIMS);
			this.offset = Cache.getOffset(row, col);
			this.value = value;
		}

		private int getOffset() {
			return offset;
		}

		private int getValue() {
			return value;
		}
	}

	public void fix(Fix f) throws DeadEndException {
		// / Fija celdas a los valores dados, y propaga cambios
		fix(f.getOffset(), f.getValue());
		heuristic();
	}

	public void fix(List<Fix> fixes) throws DeadEndException {
		// / Fija celdas a los valores dados, y propaga cambios
		for (Fix f : fixes)
			fix(f.getOffset(), f.getValue());
		heuristic();
	}

	public class CellList implements Iterator<Cell> {

		int index;
		final int[] coords;
		final int[] cells;

		private CellList(final int[] cells, final int[] coords) {
			this.cells = cells;
			this.coords = coords;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < coords.length;
		}

		@Override
		public Cell next() {
			index += 1;
			return new Cell(cells[coords[index - 1]]);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<Cell> getRow(int row) {
		return new CellList(cells, Cache.getRow(row));
	}

	public Iterator<Cell> getCol(int col) {
		return new CellList(cells, Cache.getCol(col));
	}

	public Iterator<Cell> getSquare(int square) {
		return new CellList(cells, Cache.getSquare(square));
	}

	private void rowToString(StringBuilder buffer, int row) {
		/** Vuelca una fila a texto */
		/*
		 * Vuelco la fila separando los bloques de cada cuadro con una linea
		 * horizontal, y marcando los elementos cuyo valor no esta fijado con un
		 * <0>. Alineo los digitos para que queden centrados. El modelo es algo
		 * como:
		 * 
		 * | 3 <0> 1 | <0> <0> 5 | 4 6 <0> |
		 */
		buffer.append("\n|");
		int idx = 0;
		for (int offset : Cache.getRow(row)) {
			int cell = cells[offset];
			String value = " <0> ";
			if (Cache.getLength(cell) == 1) {
				value = String.format(" %2d  ", Cache.getValue(cell));
			}
			buffer.append(value);
			idx += 1;
			if (idx == Cache.SIDE) {
				buffer.append("|");
				idx = 0;
			}
		}
	}

	public String toString() {
		/**
		 * vuelca el sudoku a texto.
		 * 
		 * Lo vuelco separando los cuadros con una fila de guiones, en forma de
		 * tabla ASCII-art.
		 * */
		StringBuilder buffer = new StringBuilder();
		StringBuilder sep = new StringBuilder("+");
		for (int i = 0; i < Cache.SIDE; i++) {
			sep.append("---------------+");
		}
		buffer.append(sep);
		sep.insert(0, "\n");
		int row = 0;
		for (int rowGroup = 0; rowGroup < Cache.SIDE; rowGroup++) {
			for (int rowOffset = 0; rowOffset < Cache.SIDE; rowOffset++, row++) {
				rowToString(buffer, row);
			}
			buffer.append(sep);
		}
		return buffer.toString();
	}

	public static Sudoku fromString(String data) throws DeadEndException {
		/**
		 * Carga un sudoku de una cadena de texto.
		 * 
		 * En la cadena de texto, cada celda del sudoku debe estar representada
		 * por uno o varios digitos en ASCII. Las celdas pueden separarse unas
		 * de otras utilizando cualquier caracter que no sea un digito ascii:
		 * espacio,s comas, guiones, saltos de linea, etc.
		 */
		List<Integer> values = new ArrayList<Integer>();
		List<Fix> fixes = new ArrayList<Fix>();
		for (String value : data.split("[^0-9]+")) {
			if (value.length() > 0)
				values.add(new Integer(value));
		}
		/*
		 * Aqui, para ser completamente independiente del orden, deberia
		 * convertir el numero de objeto en coordenadas (fila, columna) y luego
		 * llamar a Cache.getOffset()... pero aprovechando que sabemos que el
		 * orden es precisamente por fila y columna, me salto ese paso.
		 */
		for (int i = 0; i < Math.min(Cache.CELLS, values.size()); i++) {
			int value = values.get(i);
			if (value != 0)
				fixes.add(new Sudoku.Fix(i, value));
		}
		Sudoku sudoku = new Sudoku();
		sudoku.fix(fixes);
		return sudoku;
	}

	private static String readFile(String path) throws IOException {
		/** Leo el contenido del fichero a un String */
		/* Esto es lo que odio de java... en python: open(path).read() */
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

	public static void main(String[] args) {
		/**
		 * Rutina de prueba muy basica.
		 * 
		 * Carga un sudoku de un fichero de texto (especificado como primer
		 * argumento de linea de comandos), y genera un numero determinado de
		 * soluciones (especificado como segundo parametro).
		 */
		if (args.length <= 1) {
			System.err
					.println("Uso: sudoku <fichero de sudoku> [numero de resultados]\n");
			System.exit(0);
		}
		Sudoku sudoku = null;
		try {
			sudoku = Sudoku.fromString(readFile(args[1]));
		} catch (IOException exc) {
			System.err.format("No se pudo abrir el fichero %s\n", args[1]);
			System.exit(-1);
		} catch (DeadEndException exc) {
			System.err.println("El Sudoku no tiene ninguna solucion");
			System.exit(-1);
		}
		int repeat = 1000;
		int found = 0;
		Solutions sol = new Solutions(sudoku);
		if (args.length > 2)
			repeat = Math.min(repeat, Integer.parseInt(args[2]));
		long startTime = System.currentTimeMillis();
		for (; sol.hasNext() && found < repeat; found++) {
			sudoku = sol.next();
			System.out.println(sudoku.toString());
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.format(
				"%d soluciones y %d caminos sin salida encontrados en %d ms",
				found, sol.getDeadends(), elapsedTime);
		System.out.println("");
	}
}
