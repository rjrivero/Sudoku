package es.nextiraone.ing.sudoku.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;


public class Sudoku {

	/** Objeto Sudoku! */

	private final int[] cells;
	private final Cache cache;

	public Sudoku(Cache cache) {
		/** Nuevo sudoku vacio */
		this.cache = cache;
		this.cells = new int[cache.CELLS];
		for (int i = 0; i < cache.CELLS; i++) {
			cells[i] = cache.FULLCELL;
		}
	}

	protected Sudoku(final Sudoku root) {
		/** Copia de un sudoku existente */
		this.cache = root.cache;
		this.cells = Arrays.copyOf(root.cells, root.cells.length);
	}

	protected int[] getCells() {
		/** Devuelve la lista de celdas */
		return cells;
	}

	protected int getAt(int offset) {
		/** Devuelve la celda en el offset dado */
		return cells[offset];
	}

	public Cache getCache() {
		return cache;
	}

	public Cell getAt(int row, int col) {
		/** Devuelve la celda en la fila y columna dadas */
		return new Cell(this, cache.getRow(row)[col]);
	}

	private final boolean drop(int[] offsets, int used, int mask, Queue<Integer> fixedList) throws DeadEndException {
		/**
		 * Elimina un valor de la lista de valores posibles de un conjunto
		 * de celdas.
		 * 
		 * Si como resultado de la eliminacion alguna celda del conjunto
		 * queda fijada a un valor unico, agrega el offset de esa celda a
		 * la lista fixedList.
		 * 
		 * - offsets es una lista de coordenadas, de longitud <= this.getSize().
		 * - used es una bitmask, indicando las posiciones a procesar dentro del
		 *   array offsets.
		 * - mask es una bitmask con las opciones a quitar de las celdas.
		 */
		boolean changed = false;
		for(int index: cache.getOption(used)) {
			int offset = offsets[index];
			int cell   = cells[offset];
			int update = cache.getCellWithout(cell, mask);
			if(update != cell) {
				if (update == cache.EMPTYMASK) {
					/* Si llegamos a una solucion incompatible */
					throw new DeadEndException();
				}
				cells[offset] = update;
				changed = true;
				if (cache.getLength(update) == 1) {
					fixedList.add(offset);
				}
			}
		}
		return changed;
	}

	private final boolean drop(int[] offsets, int used, int mask) throws DeadEndException {
		/**
		 * Ejecuta la propagacion de cambios en el sudoku
		 * 
		 * Elimina el valor como posible de las celdas especificadas, e itera
		 * si alguna celda se ha reducido a un solo valor.
		 * 
		 * - offsets es una lista de coordenadas, de longitud <= this.getSize().
		 * - used es una bitmask, indicando las posiciones a procesar dentro del
		 *   array offsets.
		 * - mask es una bitmask con las opciones a quitar de las celdas.
		 */
		/* Esta funcion era recursiva pero la he hecho iterativa
		 * para soportar sudokus de mayor orden. Con sudokus de
		 * lado 9 la recursion maxima seria 81, que no basta para
		 * agotar el stack. Pero si el sudoku es de lado 25, la
		 * recursion podria subir hasta 625, y esa ya es una
		 * profundidad de pila peligrosa.
		 * 
		 * Obviamente para llegar a recurrir tanto tendrian que
		 * encadenarse muchas celdas que se van fijando unas a otras,
		 * lo que solo es normal al final del sudoku, cuando ya quedan
		 * pocas celdas libres.
		 * 
		 * Pero como he comprobado que pasar de recursivo a iterativo
		 * no me supone un cambio importante de rendimiento, prefiero
		 * dejarlo asi por si las moscas.
		 */
		Queue<Integer> fixedList = new ArrayDeque<Integer>();
		boolean changed = drop(offsets, used, mask, fixedList);
		while(!fixedList.isEmpty()) {
			int offset = fixedList.poll();
			int cell   = cells[offset];
			for(OffsetGroup set: cache.getNeighbor(offset)) {
				drop(set.getOffsets(), set.getMask(), cell, fixedList);
			}
		}
		return changed;
	}

	private final boolean checkCombination(int[] coords, int used, int check) throws DeadEndException {
		/** Realiza una comprobacion estadistica sobre las celdas indicadas por "check".
		 * 
		 * Cuando en un sudoku hay un grupo de "n" celdas relacionadas
		 * (de la misma fila, columna o cuadro) que en combinacion no pueden
		 * tomar mas de "n" valores, eso implica que esos valores no pueden
		 * estar en ninguna otra celda de la misma relacion (fila, columna
		 * o cuadro).
		 * 
		 * Por ejemplo, si tienes dos celdas en una fila con los posibles valores
		 * "2" y "5", no puedes saber en cual celda esta el "2" y en cual esta
		 * el "5". Lo que si que sabes es que ni el "2" ni el "5" puden estar en 
		 * ninguna otra celda de la misma fila, porque el sudoku no tendria solucion. 
		 * 
		 * Esta funcion lo que hace es valerse de eso. Comprueba los valores que
		 * tienen las celdas identificadas por "check" (es una mascara que indica
		 * que valores de "coords" estamos usando). Si entre todas combinadas no
		 * pueden tomar mas de Cache.getLength(check) valores, entonces hemos
		 * dado con una combinacion valida.
		 * 
		 * Si la combinacion es valida, aprovecha y elimina esos valores del resto
		 * de celdas del grupo. Los indices de las celdas que no esten fijas en el
		 * grupo deben indicarse mediante la mascara "used".
		 * 
		 * - offsets es una lista de coordenadas, de longitud <= this.getSize().
		 * - used es una bitmask, indicando las posiciones dentro del array de
		 *   offsets que contienen celdas no fijadas aun.
		 * - check es una bitmask, indicando las posiciones dentro del array de
		 *   offsets que estamos procesando en esta iteracion.
		 *
		 * Devuelve "true" solo si ha encontrado una combinacion valida que ademas
		 * haya tenido impacto en el sudoku (haya eliminado algun valor del
		 * resto de celdas del grupo). 
		 */
		int comb = cache.EMPTYMASK;
		for(int index: cache.getOption(check)) {
			comb = cache.getCellCombined(comb, cells[coords[index]]);
		}
		if(cache.getLength(comb) <= cache.getLength(check)) {
			return drop(coords, cache.getCellWithout(used,  check), comb);
		}
		return false;
	}

	private final boolean combineLogic(int[] coords, int used) throws DeadEndException {
		/**
		 * Fija valores por combinacion.
		 * 
		 * Comprueba las combinaciones de celdas interesantes dentro
		 * del grupo (fila, columna o cuadro) dados, utilizando la
		 * funcion checkCombination.
		 * 
		 * - offsets es una lista de coordenadas, de longitud <= this.getSize().
		 * - used es una bitmask, indicando las posiciones dentro del array de
		 *   offsets que contienen celdas no fijadas aun.
		 * - check es una bitmask, indicando las posiciones dentro del array de
		 *   offsets que estamos procesando en esta iteracion.
		 */
		/* Al igual que drop, esta funcion ha pasado de ser
		 * recursiva a iterativa.
		 */
		if(cache.getLength(used) <= 2)
			return false;
		Queue<Integer> checkList = new ArrayDeque<Integer>();
		checkList.add(used);
		while(!checkList.isEmpty()) {
			int check = checkList.poll();
			/* La combinacion dada por "check" siempre se procesa antes
			 * de meterla en el checkList (excepto en el caso inicial,
			 * ya que check == used y no tiene sentido probar nada porque
			 * no hay celdas (used - check) en las que quitar valores).
			 * 
			 * Asi que lo que nos queda es procesar los sub-grupos de "check".
			 * Sea "n" = Cache.getLength(check): Me interesan en principio
			 * los grupos de "n"-1 celdas (o menos) donde cada celda no tenga
			 * mas de "n"-1 valores. El resto de celdas no me interesan,
			 * las puedo eliminar.
			 * 
			 * Pero claro, el tema es que si quito, por ejemplo, dos celdas,
			 * ya no me interesan tampoco las celdas que pueden tomar n-1
			 * valores; solo me interesarian las que pueden tomar un maximo
			 * de n-2, asi que tendria que filtrar otra vez...
			 * 
			 * Lo que hago abajo es iterativamente ir reduciendo el
			 * conjunto de celdas interesantes. Empiezo por las que tienen
			 * mas de "n"-1 valores, pero si he conseguido reducir alguna,
			 * entonces vuelvo a intentarlo con la nueva longitud del grupo,
			 * a ver si me quito alguna otra.
			 */
			int oldcheck = check;
			int newlen = cache.getLength(check) - 1;
			int oldlen = 0;
			do {
				for(int index: cache.getOption(check)) {
					// Si la celda tiene mas de <newlen> opciones:
					if (cache.getLength(cells[coords[index]]) > newlen) {
						// entonces, la saco de la lista.
						check = cache.getMaskWithout(check, index);
					}
				}
				// y actualizo newlen
				oldlen = newlen;
				newlen = cache.getLength(check);
			}
			while (oldlen != newlen && newlen > 1);
			if (oldcheck != check) {
				/* Si he reducido la lista de opciones, la tengo que procesar
				 * para ver si la nueva lista reducida da algun resultado
				 * positivo
				 */
				if(checkCombination(coords, used, check))
					return true;
			}
			/* Si llego aqui, es porque "check" no nos ha dado una
			 * combinacion valida o que tenga efecto sobre el sudoku, pero
			 * al menos la hemos limpiado y dejado solo celdas que
			 * puedan formar subgrupos interesantes.
			 * 
			 * Asi que vamos a ir agregando esos subgrupos posiblemente
			 * interesantes a la lista de procesamiento.
			 */
			if(newlen > 2) {
				for(int index: cache.getOption(check)) {
					/* Pre-proceso el sugbrupo antes de meterlo en en la lista */
					int newcheck = cache.getMaskWithout(check, index);
					if(checkCombination(coords, used, newcheck))
						return true;
					/* Y si no hay exito, lo enlisto para que se siga buscando */
					checkList.add(newcheck);
				}
			}
		}
		return false;
	}

	private final boolean heuristicOnGroup(int[][] group) throws DeadEndException {
		/** Analiza estadisticamente un grupo de filas, columnas o cuadros */
		for (int[] coords : group) {
			/* Me quedo con la celdas no fijadas */
			int used = cache.EMPTYMASK;
			for (int idx = 0; idx < coords.length; idx++) {
				if (cache.getLength(cells[coords[idx]]) > 1)
					used = cache.getCellCombined(used, cache.getMask(idx));
			}
			/* Y las analizo por combinatoria */
			if (combineLogic(coords, used))
				return true;
		}
		return false;
	}

	public void heuristic() throws DeadEndException {
		/** Analiza estadisticamente el sudoku */
		int[][][] groups = { cache.getRows(), cache.getCols(), cache.getSquares() };
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
		int mask = cache.getMask(value - 1);
		if (!cache.doesCellContain(cells[offset], mask)) {
			/*
			 * No se puede fijar este valor en la celda porque no esta dentro de
			 * las opciones
			 */
			throw new DeadEndException();
		}
		/* fijamos la celda */
		cells[offset] = mask;
		/* y propagamos al resto de celdas */
		for (OffsetGroup set: cache.getNeighbor(offset)) {
			drop(set.getOffsets(), set.getMask(), mask);
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

	public Cell[] buildCellList(final int[] coords) {
		Cell[] out = new Cell[coords.length];
		for(int index = 0; index < coords.length; index++) {
			out[index] = new Cell(this, coords[index]);
		}
		return out;
	}

	public Cell[] getRow(int row) {
		return buildCellList(cache.getRow(row));
	}

	public Cell[] getCol(int col) {
		return buildCellList(cache.getCol(col));
	}

	public Cell[] getSquare(int square) {
		return buildCellList(cache.getSquare(square));
	}

	private final void rowToString(StringBuilder buffer, int row) {
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
		for (int offset : cache.getRow(row)) {
			int cell = cells[offset];
			String value = " <0> ";
			if (cache.getLength(cell) == 1) {
				value = String.format(" %2d  ", cache.getValue(cell));
			}
			buffer.append(value);
			idx += 1;
			if (idx == cache.SIDE) {
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
		for (int i = 0; i < cache.SIDE; i++) {
			sep.append("---------------+");
		}
		buffer.append(sep);
		sep.insert(0, "\n");
		int row = 0;
		for (int rowGroup = 0; rowGroup < cache.SIDE; rowGroup++) {
			for (int rowOffset = 0; rowOffset < cache.SIDE; rowOffset++, row++) {
				rowToString(buffer, row);
			}
			buffer.append(sep);
		}
		return buffer.toString();
	}

	public static Sudoku fromString(Cache cache, String data) throws DeadEndException {
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
		for (int i = 0; i < Math.min(cache.CELLS, values.size()); i++) {
			int value = values.get(i);
			if (value != 0)
				fixes.add(new Fix(cache, i, value));
		}
		Sudoku sudoku = new Sudoku(cache);
		sudoku.fix(fixes);
		return sudoku;
	}

	private static final String readFile(String path) throws IOException {
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
		Cache  cache  = new Cache(3);
		Sudoku sudoku = null;
		try {
			sudoku = Sudoku.fromString(cache, readFile(args[1]));
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
