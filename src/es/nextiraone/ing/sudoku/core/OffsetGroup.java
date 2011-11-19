package es.nextiraone.ing.sudoku.core;


public final class OffsetGroup {
	
	/**
	 * Un simple Bean para contener la lista de offsets de
	 * un grupo de celdas (misma fila, columna o cuadro),
	 * junto con una mascara que indica cuales de esas coordenadas
	 * se deben procesar.
	 */
	
	private final int mask;
	private final int[] offsets;
	
	public OffsetGroup(final Cache cache, final int[] offsets, int skipFrom, int skipTo) {
		int mask = cache.getMaskUpto(offsets.length);
		for(; skipFrom < skipTo; skipFrom++) {
			mask = cache.getMaskWithout(mask,  skipFrom);
		}
		this.mask    = mask;
		this.offsets = offsets;
	}
	
	public final int getMask() {
		return mask;
	}
	
	public final int[] getOffsets() {
		return offsets;
	}
}
