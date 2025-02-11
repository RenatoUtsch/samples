/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme;

import android.graphics.Bitmap;

/**
 * Wrapper class for JNI calls to ParallelME runtime. It is also responsible for
 * keeping the runtime pointer in memory so it can be used in multiple calls to
 * the low-level runtime without recreating objects.
 *
 * @author Pedro Caldeira, Wilson de Carvalho
 */
public class ParallelMERuntime {
	private static final ParallelMERuntime instance = new ParallelMERuntime();
	public final long runtimePointer;

	public static ParallelMERuntime getInstance() {
		return instance;
	}

	private native long nativeInit();
	private native void nativeCleanUpRuntime(long runtimePointer);
	private native long nativeCreateShortArray(short[] array, int length);
	private native void nativeToShortArray(long arrayPointer, short[] array);
	private native long nativeCreateIntArray(int[] array, int length);
	private native void nativeToIntArray(long arrayPointer, int[] array);
	private native long nativeCreateFloatArray(float[] array, int length);
	private native void nativeToFloatArray(long arrayPointer, float[] array);
	private native long nativeCreateBitmapImage(long runtimePointer, Bitmap bitmap, int width, int height);
	private native void nativeToBitmapBitmapImage(long runtimePointer, long imagePointer, Bitmap bitmap);
	private native long nativeCreateHDRImage(long runtimePointer, byte[] data, int width, int height);
	private native void nativeToBitmapHDRImage(long runtimePointer, long imagePointer, Bitmap bitmap);
	private native int nativeGetHeight(long imagePointer);
	private native int nativeGetWidth(long imagePointer);
	private native int nativeGetLength(long arrayPointer);

	private ParallelMERuntime() {
		System.loadLibrary("ParallelMEGenerated");
		this.runtimePointer = nativeInit();
	}

	@Override
	protected void finalize() throws Throwable {
		nativeCleanUpRuntime(runtimePointer);
		super.finalize();
	}

	public long createArray(short[] array) {
		return nativeCreateShortArray(array, array.length);
	}

	public void toArray(long arrayPointer, short[] array) {
		nativeToShortArray(arrayPointer, array);
	}

	public long createArray(int[] array) {
		return nativeCreateIntArray(array, array.length);
	}

	public void toArray(long arrayPointer, int[] array) {
		nativeToIntArray(arrayPointer, array);
	}

	public long createArray(float[] array) {
		return nativeCreateFloatArray(array, array.length);
	}

	public void toArray(long arrayPointer, float[] array) {
		nativeToFloatArray(arrayPointer, array);
	}
	
	public void createBitmapImage(Bitmap bitmap) {
		nativeCreateBitmapImage(runtimePointer, bitmap, bitmap.getWidth(), bitmap.getHeight());
	}

	public void toBitmapBitmapImage(long imagePointer, Bitmap bitmap) {
		nativeToBitmapBitmapImage(runtimePointer, imagePointer, bitmap);
	}

	public long createHDRImage(byte[] data, int width, int height) {
		return nativeCreateHDRImage(runtimePointer, data, width, height);
	}

	public void toBitmapHDRImage(long imagePointer, Bitmap bitmap) {
		nativeToBitmapHDRImage(runtimePointer, imagePointer, bitmap);
	}

	public int getHeight(long imagePointer) {
		return nativeGetHeight(imagePointer);
	}

	public int getWidth(long imagePointer) {
		return nativeGetWidth(imagePointer);
	}

	public int getLength(long arrayPointer) {
		return nativeGetLength(arrayPointer);
	}
}
