package com.gpiay.cpm.util.math;

import net.minecraft.util.math.vector.Matrix4f;

/** Encapsulates a <a href="http://en.wikipedia.org/wiki/Row-major_order#Column-major_order">column major</a> 4 by 4 matrix. Like
 * the {@link Vector3d} class it allows the chaining of methods by returning a reference to itself. For example:
 * 
 * <pre>
 * Matrix4 mat = new Matrix4().trn(position).mul(camera.combined);
 * </pre>
 * 
 * @author badlogicgames@gmail.com */
public class Matrix4d {
	/** XX: Typically the unrotated X component for scaling, also the cosine of the angle when rotated on the Y and/or Z axis. On
	 * Vector3 multiplication this value is multiplied with the source X component and added to the target X component. */
	public static final int M00 = 0;
	/** XY: Typically the negative sine of the angle when rotated on the Z axis. On Vector3 multiplication this value is multiplied
	 * with the source Y component and added to the target X component. */
	public static final int M01 = 4;
	/** XZ: Typically the sine of the angle when rotated on the Y axis. On Vector3 multiplication this value is multiplied with the
	 * source Z component and added to the target X component. */
	public static final int M02 = 8;
	/** XW: Typically the translation of the X component. On Vector3 multiplication this value is added to the target X component. */
	public static final int M03 = 12;
	/** YX: Typically the sine of the angle when rotated on the Z axis. On Vector3 multiplication this value is multiplied with the
	 * source X component and added to the target Y component. */
	public static final int M10 = 1;
	/** YY: Typically the unrotated Y component for scaling, also the cosine of the angle when rotated on the X and/or Z axis. On
	 * Vector3 multiplication this value is multiplied with the source Y component and added to the target Y component. */
	public static final int M11 = 5;
	/** YZ: Typically the negative sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied
	 * with the source Z component and added to the target Y component. */
	public static final int M12 = 9;
	/** YW: Typically the translation of the Y component. On Vector3 multiplication this value is added to the target Y component. */
	public static final int M13 = 13;
	/** ZX: Typically the negative sine of the angle when rotated on the Y axis. On Vector3 multiplication this value is multiplied
	 * with the source X component and added to the target Z component. */
	public static final int M20 = 2;
	/** ZY: Typical the sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied with the
	 * source Y component and added to the target Z component. */
	public static final int M21 = 6;
	/** ZZ: Typically the unrotated Z component for scaling, also the cosine of the angle when rotated on the X and/or Y axis. On
	 * Vector3 multiplication this value is multiplied with the source Z component and added to the target Z component. */
	public static final int M22 = 10;
	/** ZW: Typically the translation of the Z component. On Vector3 multiplication this value is added to the target Z component. */
	public static final int M23 = 14;
	/** WX: Typically the value zero. On Vector3 multiplication this value is ignored. */
	public static final int M30 = 3;
	/** WY: Typically the value zero. On Vector3 multiplication this value is ignored. */
	public static final int M31 = 7;
	/** WZ: Typically the value zero. On Vector3 multiplication this value is ignored. */
	public static final int M32 = 11;
	/** WW: Typically the value one. On Vector3 multiplication this value is ignored. */
	public static final int M33 = 15;

	static final Quat4d quat = new Quat4d();
	static final Quat4d quat2 = new Quat4d();
	static final Vector3d l_vez = new Vector3d();
	static final Vector3d l_vex = new Vector3d();
	static final Vector3d l_vey = new Vector3d();
	static final Vector3d tmpVec = new Vector3d();
	static final Matrix4d tmpMat = new Matrix4d();
	static final Vector3d right = new Vector3d();
	static final Vector3d tmpForward = new Vector3d();
	static final Vector3d tmpUp = new Vector3d();

	public final double val[] = new double[16];

	/** Constructs an identity matrix */
	public Matrix4d() {
		val[M00] = 1;
		val[M11] = 1;
		val[M22] = 1;
		val[M33] = 1;
	}

	/** Constructs a matrix from the given matrix.
	 * @param matrix The matrix to copy. (This matrix is not modified) */
	public Matrix4d(Matrix4d matrix) {
		set(matrix);
	}

	/** Constructs a matrix from the given double array. The array must have at least 16 elements; the first 16 will be copied.
	 * @param values The double array to copy. Remember that this matrix is in <a
	 *           href="http://en.wikipedia.org/wiki/Row-major_order">column major</a> order. (The double array is not modified) */
	public Matrix4d(double[] values) {
		set(values);
	}

	/** Constructs a rotation matrix from the given {@link Quat4d}.
	 * @param quat4d The quaternion to be copied. (The quaternion is not modified) */
	public Matrix4d(Quat4d quat4d) {
		set(quat4d);
	}

	/** Construct a matrix from the given translation, rotation and scale.
	 * @param position The translation
	 * @param rotation The rotation, must be normalized
	 * @param scale The scale */
	public Matrix4d(Vector3d position, Quat4d rotation, Vector3d scale) {
		set(position, rotation, scale);
	}

	public Matrix4d(Matrix4f matrix) {
		val[M00] = matrix.m00; val[M01] = matrix.m01; val[M02] = matrix.m02; val[M03] = matrix.m03;
		val[M10] = matrix.m10; val[M11] = matrix.m11; val[M12] = matrix.m12; val[M13] = matrix.m13;
		val[M20] = matrix.m20; val[M21] = matrix.m21; val[M22] = matrix.m22; val[M23] = matrix.m23;
		val[M30] = matrix.m30; val[M31] = matrix.m31; val[M32] = matrix.m32; val[M33] = matrix.m33;
	}

	/** Sets the matrix to the given matrix.
	 * @param matrix The matrix that is to be copied. (The given matrix is not modified)
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (Matrix4d matrix) {
		return set(matrix.val);
	}

	/** Sets the matrix to the given matrix as a double array. The double array must have at least 16 elements; the first 16 will be
	 * copied.
	 * 
	 * @param values The matrix, in double form, that is to be copied. Remember that this matrix is in <a
	 *           href="http://en.wikipedia.org/wiki/Row-major_order">column major</a> order.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (double[] values) {
		System.arraycopy(values, 0, val, 0, val.length);
		return this;
	}

	/** Sets the matrix to a rotation matrix representing the quaternion.
	 * @param quat4d The quaternion that is to be used to set this matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (Quat4d quat4d) {
		return set(quat4d.x, quat4d.y, quat4d.z, quat4d.w);
	}

	/** Sets the matrix to a rotation matrix representing the quaternion.
	 * 
	 * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
	 * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
	 * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
	 * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (double quaternionX, double quaternionY, double quaternionZ, double quaternionW) {
		return set(0, 0, 0, quaternionX, quaternionY, quaternionZ, quaternionW);
	}

	/** Set this matrix to the specified translation and rotation.
	 * @param position The translation
	 * @param orientation The rotation, must be normalized
	 * @return This matrix for chaining */
	public Matrix4d set (Vector3d position, Quat4d orientation) {
		return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w);
	}

	/** Sets the matrix to a rotation matrix representing the translation and quaternion.
	 * @param translationX The X component of the translation that is to be used to set this matrix.
	 * @param translationY The Y component of the translation that is to be used to set this matrix.
	 * @param translationZ The Z component of the translation that is to be used to set this matrix.
	 * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
	 * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
	 * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
	 * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (double translationX, double translationY, double translationZ, double quaternionX, double quaternionY,
						 double quaternionZ, double quaternionW) {
		final double xs = quaternionX * 2, ys = quaternionY * 2, zs = quaternionZ * 2;
		final double wx = quaternionW * xs, wy = quaternionW * ys, wz = quaternionW * zs;
		final double xx = quaternionX * xs, xy = quaternionX * ys, xz = quaternionX * zs;
		final double yy = quaternionY * ys, yz = quaternionY * zs, zz = quaternionZ * zs;

		val[M00] = 1 - (yy + zz);
		val[M01] = xy - wz;
		val[M02] = xz + wy;
		val[M03] = translationX;

		val[M10] = xy + wz;
		val[M11] = 1 - (xx + zz);
		val[M12] = yz - wx;
		val[M13] = translationY;

		val[M20] = xz - wy;
		val[M21] = yz + wx;
		val[M22] = 1 - (xx + yy);
		val[M23] = translationZ;

		val[M30] = 0;
		val[M31] = 0;
		val[M32] = 0;
		val[M33] = 1;
		return this;
	}

	/** Set this matrix to the specified translation, rotation and scale.
	 * @param position The translation
	 * @param orientation The rotation, must be normalized
	 * @param scale The scale
	 * @return This matrix for chaining */
	public Matrix4d set (Vector3d position, Quat4d orientation, Vector3d scale) {
		return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w, scale.x,
			scale.y, scale.z);
	}

	/** Sets the matrix to a rotation matrix representing the translation and quaternion.
	 * @param translationX The X component of the translation that is to be used to set this matrix.
	 * @param translationY The Y component of the translation that is to be used to set this matrix.
	 * @param translationZ The Z component of the translation that is to be used to set this matrix.
	 * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
	 * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
	 * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
	 * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
	 * @param scaleX The X component of the scaling that is to be used to set this matrix.
	 * @param scaleY The Y component of the scaling that is to be used to set this matrix.
	 * @param scaleZ The Z component of the scaling that is to be used to set this matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d set (double translationX, double translationY, double translationZ, double quaternionX, double quaternionY,
						 double quaternionZ, double quaternionW, double scaleX, double scaleY, double scaleZ) {
		final double xs = quaternionX * 2, ys = quaternionY * 2, zs = quaternionZ * 2;
		final double wx = quaternionW * xs, wy = quaternionW * ys, wz = quaternionW * zs;
		final double xx = quaternionX * xs, xy = quaternionX * ys, xz = quaternionX * zs;
		final double yy = quaternionY * ys, yz = quaternionY * zs, zz = quaternionZ * zs;

		val[M00] = scaleX * (1.0 - (yy + zz));
		val[M01] = scaleY * (xy - wz);
		val[M02] = scaleZ * (xz + wy);
		val[M03] = translationX;

		val[M10] = scaleX * (xy + wz);
		val[M11] = scaleY * (1.0 - (xx + zz));
		val[M12] = scaleZ * (yz - wx);
		val[M13] = translationY;

		val[M20] = scaleX * (xz - wy);
		val[M21] = scaleY * (yz + wx);
		val[M22] = scaleZ * (1.0 - (xx + yy));
		val[M23] = translationZ;

		val[M30] = 0;
		val[M31] = 0;
		val[M32] = 0;
		val[M33] = 1;
		return this;
	}

	/** Sets the four columns of the matrix which correspond to the x-, y- and z-axis of the vector space this matrix creates as
	 * well as the 4th column representing the translation of any point that is multiplied by this matrix.
	 * @param xAxis The x-axis.
	 * @param yAxis The y-axis.
	 * @param zAxis The z-axis.
	 * @param pos The translation vector. */
	public Matrix4d set (Vector3d xAxis, Vector3d yAxis, Vector3d zAxis, Vector3d pos) {
		val[M00] = xAxis.x;
		val[M01] = xAxis.y;
		val[M02] = xAxis.z;
		val[M10] = yAxis.x;
		val[M11] = yAxis.y;
		val[M12] = yAxis.z;
		val[M20] = zAxis.x;
		val[M21] = zAxis.y;
		val[M22] = zAxis.z;
		val[M03] = pos.x;
		val[M13] = pos.y;
		val[M23] = pos.z;
		val[M30] = 0;
		val[M31] = 0;
		val[M32] = 0;
		val[M33] = 1;
		return this;
	}

	/** @return a copy of this matrix */
	public Matrix4d cpy () {
		return new Matrix4d(this);
	}

	/** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
	 * @param vector The translation vector to add to the current matrix. (This vector is not modified)
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d trn (Vector3d vector) {
		val[M03] += vector.x;
		val[M13] += vector.y;
		val[M23] += vector.z;
		return this;
	}

	/** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @param z The z-component of the translation vector.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d trn (double x, double y, double z) {
		val[M03] += x;
		val[M13] += y;
		val[M23] += z;
		return this;
	}

	/** @return the backing double array */
	public double[] getValues () {
		return val;
	}

	/** Postmultiplies this matrix with the given matrix, storing the result in this matrix. For example:
	 * 
	 * <pre>
	 * A.mul(B) results in A := AB.
	 * </pre>
	 * 
	 * @param matrix The other matrix to multiply by.
	 * @return This matrix for the purpose of chaining operations together. */
	public Matrix4d mul (Matrix4d matrix) {
		mul(val, matrix.val);
		return this;
	}

	/** Premultiplies this matrix with the given matrix, storing the result in this matrix. For example:
	 * 
	 * <pre>
	 * A.mulLeft(B) results in A := BA.
	 * </pre>
	 * 
	 * @param matrix The other matrix to multiply by.
	 * @return This matrix for the purpose of chaining operations together. */
	public Matrix4d mulLeft (Matrix4d matrix) {
		tmpMat.set(matrix);
		mul(tmpMat.val, val);
		return set(tmpMat);
	}

	/** Transposes the matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d tra () {
		double m01 = val[M01];
		double m02 = val[M02];
		double m03 = val[M03];
		double m12 = val[M12];
		double m13 = val[M13];
		double m23 = val[M23];
		val[M01] = val[M10];
		val[M02] = val[M20];
		val[M03] = val[M30];
		val[M10] = m01;
		val[M12] = val[M21];
		val[M13] = val[M31];
		val[M20] = m02;
		val[M21] = m12;
		val[M23] = val[M32];
		val[M30] = m03;
		val[M31] = m13;
		val[M32] = m23;
		return this;
	}

	/** Sets the matrix to an identity matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d idt () {
		val[M00] = 1;
		val[M01] = 0;
		val[M02] = 0;
		val[M03] = 0;
		val[M10] = 0;
		val[M11] = 1;
		val[M12] = 0;
		val[M13] = 0;
		val[M20] = 0;
		val[M21] = 0;
		val[M22] = 1;
		val[M23] = 0;
		val[M30] = 0;
		val[M31] = 0;
		val[M32] = 0;
		val[M33] = 1;
		return this;
	}

	/** Inverts the matrix. Stores the result in this matrix.
	 * @return This matrix for the purpose of chaining methods together.
	 * @throws RuntimeException if the matrix is singular (not invertible) */
	public Matrix4d inv () {
		double l_det = val[M30] * val[M21] * val[M12] * val[M03] - val[M20] * val[M31] * val[M12] * val[M03] - val[M30] * val[M11]
			* val[M22] * val[M03] + val[M10] * val[M31] * val[M22] * val[M03] + val[M20] * val[M11] * val[M32] * val[M03] - val[M10]
			* val[M21] * val[M32] * val[M03] - val[M30] * val[M21] * val[M02] * val[M13] + val[M20] * val[M31] * val[M02] * val[M13]
			+ val[M30] * val[M01] * val[M22] * val[M13] - val[M00] * val[M31] * val[M22] * val[M13] - val[M20] * val[M01] * val[M32]
			* val[M13] + val[M00] * val[M21] * val[M32] * val[M13] + val[M30] * val[M11] * val[M02] * val[M23] - val[M10] * val[M31]
			* val[M02] * val[M23] - val[M30] * val[M01] * val[M12] * val[M23] + val[M00] * val[M31] * val[M12] * val[M23] + val[M10]
			* val[M01] * val[M32] * val[M23] - val[M00] * val[M11] * val[M32] * val[M23] - val[M20] * val[M11] * val[M02] * val[M33]
			+ val[M10] * val[M21] * val[M02] * val[M33] + val[M20] * val[M01] * val[M12] * val[M33] - val[M00] * val[M21] * val[M12]
			* val[M33] - val[M10] * val[M01] * val[M22] * val[M33] + val[M00] * val[M11] * val[M22] * val[M33];
		if (l_det == 0) throw new RuntimeException("non-invertible matrix");
		double m00 = val[M12] * val[M23] * val[M31] - val[M13] * val[M22] * val[M31] + val[M13] * val[M21] * val[M32]
			- val[M11] * val[M23] * val[M32] - val[M12] * val[M21] * val[M33] + val[M11] * val[M22] * val[M33];
		double m01 = val[M03] * val[M22] * val[M31] - val[M02] * val[M23] * val[M31] - val[M03] * val[M21] * val[M32]
			+ val[M01] * val[M23] * val[M32] + val[M02] * val[M21] * val[M33] - val[M01] * val[M22] * val[M33];
		double m02 = val[M02] * val[M13] * val[M31] - val[M03] * val[M12] * val[M31] + val[M03] * val[M11] * val[M32]
			- val[M01] * val[M13] * val[M32] - val[M02] * val[M11] * val[M33] + val[M01] * val[M12] * val[M33];
		double m03 = val[M03] * val[M12] * val[M21] - val[M02] * val[M13] * val[M21] - val[M03] * val[M11] * val[M22]
			+ val[M01] * val[M13] * val[M22] + val[M02] * val[M11] * val[M23] - val[M01] * val[M12] * val[M23];
		double m10 = val[M13] * val[M22] * val[M30] - val[M12] * val[M23] * val[M30] - val[M13] * val[M20] * val[M32]
			+ val[M10] * val[M23] * val[M32] + val[M12] * val[M20] * val[M33] - val[M10] * val[M22] * val[M33];
		double m11 = val[M02] * val[M23] * val[M30] - val[M03] * val[M22] * val[M30] + val[M03] * val[M20] * val[M32]
			- val[M00] * val[M23] * val[M32] - val[M02] * val[M20] * val[M33] + val[M00] * val[M22] * val[M33];
		double m12 = val[M03] * val[M12] * val[M30] - val[M02] * val[M13] * val[M30] - val[M03] * val[M10] * val[M32]
			+ val[M00] * val[M13] * val[M32] + val[M02] * val[M10] * val[M33] - val[M00] * val[M12] * val[M33];
		double m13 = val[M02] * val[M13] * val[M20] - val[M03] * val[M12] * val[M20] + val[M03] * val[M10] * val[M22]
			- val[M00] * val[M13] * val[M22] - val[M02] * val[M10] * val[M23] + val[M00] * val[M12] * val[M23];
		double m20 = val[M11] * val[M23] * val[M30] - val[M13] * val[M21] * val[M30] + val[M13] * val[M20] * val[M31]
			- val[M10] * val[M23] * val[M31] - val[M11] * val[M20] * val[M33] + val[M10] * val[M21] * val[M33];
		double m21 = val[M03] * val[M21] * val[M30] - val[M01] * val[M23] * val[M30] - val[M03] * val[M20] * val[M31]
			+ val[M00] * val[M23] * val[M31] + val[M01] * val[M20] * val[M33] - val[M00] * val[M21] * val[M33];
		double m22 = val[M01] * val[M13] * val[M30] - val[M03] * val[M11] * val[M30] + val[M03] * val[M10] * val[M31]
			- val[M00] * val[M13] * val[M31] - val[M01] * val[M10] * val[M33] + val[M00] * val[M11] * val[M33];
		double m23 = val[M03] * val[M11] * val[M20] - val[M01] * val[M13] * val[M20] - val[M03] * val[M10] * val[M21]
			+ val[M00] * val[M13] * val[M21] + val[M01] * val[M10] * val[M23] - val[M00] * val[M11] * val[M23];
		double m30 = val[M12] * val[M21] * val[M30] - val[M11] * val[M22] * val[M30] - val[M12] * val[M20] * val[M31]
			+ val[M10] * val[M22] * val[M31] + val[M11] * val[M20] * val[M32] - val[M10] * val[M21] * val[M32];
		double m31 = val[M01] * val[M22] * val[M30] - val[M02] * val[M21] * val[M30] + val[M02] * val[M20] * val[M31]
			- val[M00] * val[M22] * val[M31] - val[M01] * val[M20] * val[M32] + val[M00] * val[M21] * val[M32];
		double m32 = val[M02] * val[M11] * val[M30] - val[M01] * val[M12] * val[M30] - val[M02] * val[M10] * val[M31]
			+ val[M00] * val[M12] * val[M31] + val[M01] * val[M10] * val[M32] - val[M00] * val[M11] * val[M32];
		double m33 = val[M01] * val[M12] * val[M20] - val[M02] * val[M11] * val[M20] + val[M02] * val[M10] * val[M21]
			- val[M00] * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] + val[M00] * val[M11] * val[M22];
		double inv_det = 1.0 / l_det;
		val[M00] = m00 * inv_det;
		val[M10] = m10 * inv_det;
		val[M20] = m20 * inv_det;
		val[M30] = m30 * inv_det;
		val[M01] = m01 * inv_det;
		val[M11] = m11 * inv_det;
		val[M21] = m21 * inv_det;
		val[M31] = m31 * inv_det;
		val[M02] = m02 * inv_det;
		val[M12] = m12 * inv_det;
		val[M22] = m22 * inv_det;
		val[M32] = m32 * inv_det;
		val[M03] = m03 * inv_det;
		val[M13] = m13 * inv_det;
		val[M23] = m23 * inv_det;
		val[M33] = m33 * inv_det;
		return this;
	}

	/** @return The determinant of this matrix */
	public double det () {
		return val[M30] * val[M21] * val[M12] * val[M03] - val[M20] * val[M31] * val[M12] * val[M03] - val[M30] * val[M11]
			* val[M22] * val[M03] + val[M10] * val[M31] * val[M22] * val[M03] + val[M20] * val[M11] * val[M32] * val[M03] - val[M10]
			* val[M21] * val[M32] * val[M03] - val[M30] * val[M21] * val[M02] * val[M13] + val[M20] * val[M31] * val[M02] * val[M13]
			+ val[M30] * val[M01] * val[M22] * val[M13] - val[M00] * val[M31] * val[M22] * val[M13] - val[M20] * val[M01] * val[M32]
			* val[M13] + val[M00] * val[M21] * val[M32] * val[M13] + val[M30] * val[M11] * val[M02] * val[M23] - val[M10] * val[M31]
			* val[M02] * val[M23] - val[M30] * val[M01] * val[M12] * val[M23] + val[M00] * val[M31] * val[M12] * val[M23] + val[M10]
			* val[M01] * val[M32] * val[M23] - val[M00] * val[M11] * val[M32] * val[M23] - val[M20] * val[M11] * val[M02] * val[M33]
			+ val[M10] * val[M21] * val[M02] * val[M33] + val[M20] * val[M01] * val[M12] * val[M33] - val[M00] * val[M21] * val[M12]
			* val[M33] - val[M10] * val[M01] * val[M22] * val[M33] + val[M00] * val[M11] * val[M22] * val[M33];
	}

	/** @return The determinant of the 3x3 upper left matrix */
	public double det3x3 () {
		return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00]
			* val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
	}

	/** Sets the matrix to a projection matrix with a near- and far plane, a field of view in degrees and an aspect ratio. Note that
	 * the field of view specified is the angle in degrees for the height, the field of view for the width will be calculated
	 * according to the aspect ratio.
	 * @param near The near plane
	 * @param far The far plane
	 * @param fovy The field of view of the height in degrees
	 * @param aspectRatio The "width over height" aspect ratio
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToProjection (double near, double far, double fovy, double aspectRatio) {
		idt();
		double l_fd = 1.0 / Math.tan((fovy * (Math.PI / 180)) / 2.0);
		double l_a1 = (far + near) / (near - far);
		double l_a2 = (2 * far * near) / (near - far);
		val[M00] = l_fd / aspectRatio;
		val[M10] = 0;
		val[M20] = 0;
		val[M30] = 0;
		val[M01] = 0;
		val[M11] = l_fd;
		val[M21] = 0;
		val[M31] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = l_a1;
		val[M32] = -1;
		val[M03] = 0;
		val[M13] = 0;
		val[M23] = l_a2;
		val[M33] = 0;
		return this;
	}

	/** Sets the matrix to a projection matrix with a near/far plane, and left, bottom, right and top specifying the points on the
	 * near plane that are mapped to the lower left and upper right corners of the viewport. This allows to create projection
	 * matrix with off-center vanishing point.
	 * @param left
	 * @param right
	 * @param bottom
	 * @param top
	 * @param near The near plane
	 * @param far The far plane
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToProjection (double left, double right, double bottom, double top, double near, double far) {
		double x = 2.0 * near / (right - left);
		double y = 2.0 * near / (top - bottom);
		double a = (right + left) / (right - left);
		double b = (top + bottom) / (top - bottom);
		double l_a1 = (far + near) / (near - far);
		double l_a2 = (2 * far * near) / (near - far);
		val[M00] = x;
		val[M10] = 0;
		val[M20] = 0;
		val[M30] = 0;
		val[M01] = 0;
		val[M11] = y;
		val[M21] = 0;
		val[M31] = 0;
		val[M02] = a;
		val[M12] = b;
		val[M22] = l_a1;
		val[M32] = -1;
		val[M03] = 0;
		val[M13] = 0;
		val[M23] = l_a2;
		val[M33] = 0;
		return this;
	}

	/** Sets this matrix to an orthographic projection matrix with the origin at (x,y) extending by width and height. The near plane
	 * is set to 0, the far plane is set to 1.
	 * @param x The x-coordinate of the origin
	 * @param y The y-coordinate of the origin
	 * @param width The width
	 * @param height The height
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToOrtho2D (double x, double y, double width, double height) {
		setToOrtho(x, x + width, y, y + height, 0, 1);
		return this;
	}

	/** Sets this matrix to an orthographic projection matrix with the origin at (x,y) extending by width and height, having a near
	 * and far plane.
	 * @param x The x-coordinate of the origin
	 * @param y The y-coordinate of the origin
	 * @param width The width
	 * @param height The height
	 * @param near The near plane
	 * @param far The far plane
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToOrtho2D (double x, double y, double width, double height, double near, double far) {
		setToOrtho(x, x + width, y, y + height, near, far);
		return this;
	}

	/** Sets the matrix to an orthographic projection like glOrtho (http://www.opengl.org/sdk/docs/man/xhtml/glOrtho.xml) following
	 * the OpenGL equivalent
	 * @param left The left clipping plane
	 * @param right The right clipping plane
	 * @param bottom The bottom clipping plane
	 * @param top The top clipping plane
	 * @param near The near clipping plane
	 * @param far The far clipping plane
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToOrtho (double left, double right, double bottom, double top, double near, double far) {
		double x_orth = 2 / (right - left);
		double y_orth = 2 / (top - bottom);
		double z_orth = -2 / (far - near);

		double tx = -(right + left) / (right - left);
		double ty = -(top + bottom) / (top - bottom);
		double tz = -(far + near) / (far - near);

		val[M00] = x_orth;
		val[M10] = 0;
		val[M20] = 0;
		val[M30] = 0;
		val[M01] = 0;
		val[M11] = y_orth;
		val[M21] = 0;
		val[M31] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = z_orth;
		val[M32] = 0;
		val[M03] = tx;
		val[M13] = ty;
		val[M23] = tz;
		val[M33] = 1;
		return this;
	}

	/** Sets the 4th column to the translation vector.
	 * @param vector The translation vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setTranslation (Vector3d vector) {
		val[M03] = vector.x;
		val[M13] = vector.y;
		val[M23] = vector.z;
		return this;
	}

	/** Sets the 4th column to the translation vector.
	 * @param x The X coordinate of the translation vector
	 * @param y The Y coordinate of the translation vector
	 * @param z The Z coordinate of the translation vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setTranslation (double x, double y, double z) {
		val[M03] = x;
		val[M13] = y;
		val[M23] = z;
		return this;
	}

	/** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
	 * translation vector.
	 * @param vector The translation vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToTranslation (Vector3d vector) {
		idt();
		val[M03] = vector.x;
		val[M13] = vector.y;
		val[M23] = vector.z;
		return this;
	}

	/** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
	 * translation vector.
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @param z The z-component of the translation vector.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToTranslation (double x, double y, double z) {
		idt();
		val[M03] = x;
		val[M13] = y;
		val[M23] = z;
		return this;
	}

	/** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
	 * translation vector in the 4th column and the scaling vector in the diagonal.
	 * @param translation The translation vector
	 * @param scaling The scaling vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToTranslationAndScaling (Vector3d translation, Vector3d scaling) {
		idt();
		val[M03] = translation.x;
		val[M13] = translation.y;
		val[M23] = translation.z;
		val[M00] = scaling.x;
		val[M11] = scaling.y;
		val[M22] = scaling.z;
		return this;
	}

	/** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
	 * translation vector in the 4th column and the scaling vector in the diagonal.
	 * @param translationX The x-component of the translation vector
	 * @param translationY The y-component of the translation vector
	 * @param translationZ The z-component of the translation vector
	 * @param scalingX The x-component of the scaling vector
	 * @param scalingY The x-component of the scaling vector
	 * @param scalingZ The x-component of the scaling vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToTranslationAndScaling (double translationX, double translationY, double translationZ, double scalingX,
												double scalingY, double scalingZ) {
		idt();
		val[M03] = translationX;
		val[M13] = translationY;
		val[M23] = translationZ;
		val[M00] = scalingX;
		val[M11] = scalingY;
		val[M22] = scalingZ;
		return this;
	}

	/** Sets the matrix to a rotation matrix around the given axis.
	 * @param axis The axis
	 * @param degrees The angle in degrees
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToRotation (Vector3d axis, double degrees) {
		if (degrees == 0) {
			idt();
			return this;
		}
		return set(quat.set(axis, degrees));
	}

	/** Sets the matrix to a rotation matrix around the given axis.
	 * @param axis The axis
	 * @param radians The angle in radians
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToRotationRad (Vector3d axis, double radians) {
		if (radians == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxisRad(axis, radians));
	}

	/** Sets the matrix to a rotation matrix around the given axis.
	 * @param axisX The x-component of the axis
	 * @param axisY The y-component of the axis
	 * @param axisZ The z-component of the axis
	 * @param degrees The angle in degrees
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToRotation (double axisX, double axisY, double axisZ, double degrees) {
		if (degrees == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxis(axisX, axisY, axisZ, degrees));
	}

	/** Sets the matrix to a rotation matrix around the given axis.
	 * @param axisX The x-component of the axis
	 * @param axisY The y-component of the axis
	 * @param axisZ The z-component of the axis
	 * @param radians The angle in radians
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToRotationRad (double axisX, double axisY, double axisZ, double radians) {
		if (radians == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxisRad(axisX, axisY, axisZ, radians));
	}

	/** Set the matrix to a rotation matrix between two vectors.
	 * @param v1 The base vector
	 * @param v2 The target vector
	 * @return This matrix for the purpose of chaining methods together */
	public Matrix4d setToRotation (final Vector3d v1, final Vector3d v2) {
		return set(quat.setFromCross(v1, v2));
	}

	/** Set the matrix to a rotation matrix between two vectors.
	 * @param x1 The base vectors x value
	 * @param y1 The base vectors y value
	 * @param z1 The base vectors z value
	 * @param x2 The target vector x value
	 * @param y2 The target vector y value
	 * @param z2 The target vector z value
	 * @return This matrix for the purpose of chaining methods together */
	public Matrix4d setToRotation (final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		return set(quat.setFromCross(x1, y1, z1, x2, y2, z2));
	}

	/** Sets this matrix to a rotation matrix from the given euler angles.
	 * @param yaw the yaw in degrees
	 * @param pitch the pitch in degrees
	 * @param roll the roll in degrees
	 * @return This matrix */
	public Matrix4d setFromEulerAngles (double yaw, double pitch, double roll) {
		quat.setEulerAngles(yaw, pitch, roll);
		return set(quat);
	}
	
	/** Sets this matrix to a rotation matrix from the given euler angles.
	 * @param yaw the yaw in radians
	 * @param pitch the pitch in radians
	 * @param roll the roll in radians
	 * @return This matrix */
	public Matrix4d setFromEulerAnglesRad (double yaw, double pitch, double roll) {
		quat.setEulerAnglesRad(yaw, pitch, roll);
		return set(quat);
	}

	/** Sets this matrix to a scaling matrix
	 * @param vector The scaling vector
	 * @return This matrix for chaining. */
	public Matrix4d setToScaling (Vector3d vector) {
		idt();
		val[M00] = vector.x;
		val[M11] = vector.y;
		val[M22] = vector.z;
		return this;
	}

	/** Sets this matrix to a scaling matrix
	 * @param x The x-component of the scaling vector
	 * @param y The y-component of the scaling vector
	 * @param z The z-component of the scaling vector
	 * @return This matrix for chaining. */
	public Matrix4d setToScaling (double x, double y, double z) {
		idt();
		val[M00] = x;
		val[M11] = y;
		val[M22] = z;
		return this;
	}


	/** Sets the matrix to a look at matrix with a direction and an up vector. Multiply with a translation matrix to get a camera
	 * model view matrix.
	 * @param direction The direction vector
	 * @param up The up vector
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d setToLookAt (Vector3d direction, Vector3d up) {
		l_vez.set(direction).nor();
		l_vex.set(direction).crs(up).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
		idt();
		val[M00] = l_vex.x;
		val[M01] = l_vex.y;
		val[M02] = l_vex.z;
		val[M10] = l_vey.x;
		val[M11] = l_vey.y;
		val[M12] = l_vey.z;
		val[M20] = -l_vez.x;
		val[M21] = -l_vez.y;
		val[M22] = -l_vez.z;
		return this;
	}


	/** Sets this matrix to a look at matrix with the given position, target and up vector.
	 * @param position the position
	 * @param target the target
	 * @param up the up vector
	 * @return This matrix */
	public Matrix4d setToLookAt (Vector3d position, Vector3d target, Vector3d up) {
		tmpVec.set(target).sub(position);
		setToLookAt(tmpVec, up);
		mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
		return this;
	}

	
	public Matrix4d setToWorld (Vector3d position, Vector3d forward, Vector3d up) {
		tmpForward.set(forward).nor();
		right.set(tmpForward).crs(up).nor();
		tmpUp.set(right).crs(tmpForward).nor();
		set(right, tmpUp, tmpForward.scl(-1), position);
		return this;
	}

	/** Linearly interpolates between this matrix and the given matrix mixing by alpha
	 * @param matrix the matrix
	 * @param alpha the alpha value in the range [0,1]
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d lerp (Matrix4d matrix, double alpha) {
		for (int i = 0; i < 16; i++)
			val[i] = val[i] * (1 - alpha) + matrix.val[i] * alpha;
		return this;
	}

	/** Averages the given transform with this one and stores the result in this matrix. Translations and scales are lerped while
	 * rotations are slerped.
	 * @param other The other transform
	 * @param w Weight of this transform; weight of the other transform is (1 - w)
	 * @return This matrix for chaining */
	public Matrix4d avg (Matrix4d other, double w) {
		getScale(tmpVec);
		other.getScale(tmpForward);

		getRotation(quat);
		other.getRotation(quat2);

		getTranslation(tmpUp);
		other.getTranslation(right);

		setToScaling(tmpVec.scl(w).add(tmpForward.scl(1 - w)));
		rotate(quat.slerp(quat2, 1 - w));
		setTranslation(tmpUp.scl(w).add(right.scl(1 - w)));
		return this;
	}

	/** Averages the given transforms and stores the result in this matrix. Translations and scales are lerped while rotations are
	 * slerped. Does not destroy the data contained in t.
	 * @param t List of transforms
	 * @return This matrix for chaining */
	public Matrix4d avg (Matrix4d[] t) {
		final double w = 1.0 / t.length;

		tmpVec.set(t[0].getScale(tmpUp).scl(w));
		quat.set(t[0].getRotation(quat2).exp(w));
		tmpForward.set(t[0].getTranslation(tmpUp).scl(w));

		for (int i = 1; i < t.length; i++) {
			tmpVec.add(t[i].getScale(tmpUp).scl(w));
			quat.mul(t[i].getRotation(quat2).exp(w));
			tmpForward.add(t[i].getTranslation(tmpUp).scl(w));
		}
		quat.nor();

		setToScaling(tmpVec);
		rotate(quat);
		setTranslation(tmpForward);
		return this;
	}

	/** Averages the given transforms with the given weights and stores the result in this matrix. Translations and scales are
	 * lerped while rotations are slerped. Does not destroy the data contained in t or w; Sum of w_i must be equal to 1, or
	 * unexpected results will occur.
	 * @param t List of transforms
	 * @param w List of weights
	 * @return This matrix for chaining */
	public Matrix4d avg (Matrix4d[] t, double[] w) {
		tmpVec.set(t[0].getScale(tmpUp).scl(w[0]));
		quat.set(t[0].getRotation(quat2).exp(w[0]));
		tmpForward.set(t[0].getTranslation(tmpUp).scl(w[0]));

		for (int i = 1; i < t.length; i++) {
			tmpVec.add(t[i].getScale(tmpUp).scl(w[i]));
			quat.mul(t[i].getRotation(quat2).exp(w[i]));
			tmpForward.add(t[i].getTranslation(tmpUp).scl(w[i]));
		}
		quat.nor();

		setToScaling(tmpVec);
		rotate(quat);
		setTranslation(tmpForward);
		return this;
	}

	/** Sets this matrix to the given 3x3 matrix. The third column of this matrix is set to (0,0,1,0).
	 * @param mat the matrix */
	public Matrix4d set (Matrix3d mat) {
		val[0] = mat.val[0];
		val[1] = mat.val[1];
		val[2] = mat.val[2];
		val[3] = 0;
		val[4] = mat.val[3];
		val[5] = mat.val[4];
		val[6] = mat.val[5];
		val[7] = 0;
		val[8] = 0;
		val[9] = 0;
		val[10] = 1;
		val[11] = 0;
		val[12] = mat.val[6];
		val[13] = mat.val[7];
		val[14] = 0;
		val[15] = mat.val[8];
		return this;
	}

	/** Assumes that both matrices are 2D affine transformations, copying only the relevant components. The copied values are:
	 *
	 * <pre>
	 *      [  M00  M01   _   M03  ]
	 *      [  M10  M11   _   M13  ]
	 *      [   _    _    _    _   ]
	 *      [   _    _    _    _   ]
	 * </pre>
	 * @param mat the source matrix
	 * @return This matrix for chaining */
	public Matrix4d setAsAffine (Matrix4d mat) {
		val[M00] = mat.val[M00];
		val[M10] = mat.val[M10];
		val[M01] = mat.val[M01];
		val[M11] = mat.val[M11];
		val[M03] = mat.val[M03];
		val[M13] = mat.val[M13];
		return this;
	}

	public Matrix4d scl (Vector3d scale) {
		val[M00] *= scale.x;
		val[M11] *= scale.y;
		val[M22] *= scale.z;
		return this;
	}

	public Matrix4d scl (double x, double y, double z) {
		val[M00] *= x;
		val[M11] *= y;
		val[M22] *= z;
		return this;
	}

	public Matrix4d scl (double scale) {
		val[M00] *= scale;
		val[M11] *= scale;
		val[M22] *= scale;
		return this;
	}

	public Vector3d getTranslation (Vector3d position) {
		position.x = val[M03];
		position.y = val[M13];
		position.z = val[M23];
		return position;
	}

	/** Gets the rotation of this matrix.
	 * @param rotation The {@link Quat4d} to receive the rotation
	 * @param normalizeAxes True to normalize the axes, necessary when the matrix might also include scaling.
	 * @return The provided {@link Quat4d} for chaining. */
	public Quat4d getRotation (Quat4d rotation, boolean normalizeAxes) {
		return rotation.setFromMatrix(normalizeAxes, this);
	}

	/** Gets the rotation of this matrix.
	 * @param rotation The {@link Quat4d} to receive the rotation
	 * @return The provided {@link Quat4d} for chaining. */
	public Quat4d getRotation (Quat4d rotation) {
		return rotation.setFromMatrix(this);
	}

	/** @return the squared scale factor on the X axis */
	public double getScaleXSquared () {
		return val[M00] * val[M00] + val[M01] * val[M01] + val[M02] * val[M02];
	}

	/** @return the squared scale factor on the Y axis */
	public double getScaleYSquared () {
		return val[M10] * val[M10] + val[M11] * val[M11] + val[M12] * val[M12];
	}

	/** @return the squared scale factor on the Z axis */
	public double getScaleZSquared () {
		return val[M20] * val[M20] + val[M21] * val[M21] + val[M22] * val[M22];
	}

	/** @return the scale factor on the X axis (non-negative) */
	public double getScaleX () {
		return (Math.abs(val[M01]) < 1e-5 && Math.abs(val[M02]) < 1e-5) ? Math.abs(val[M00])
			: Math.sqrt(getScaleXSquared());
	}

	/** @return the scale factor on the Y axis (non-negative) */
	public double getScaleY () {
		return (Math.abs(val[M10]) < 1e-5 && Math.abs(val[M12]) < 1e-5) ? Math.abs(val[M11])
			: Math.sqrt(getScaleYSquared());
	}

	/** @return the scale factor on the X axis (non-negative) */
	public double getScaleZ () {
		return (Math.abs(val[M20]) < 1e-5 && Math.abs(val[M21]) < 1e-5) ? Math.abs(val[M22])
			: Math.sqrt(getScaleZSquared());
	}

	/** @param scale The vector which will receive the (non-negative) scale components on each axis.
	 * @return The provided vector for chaining. */
	public Vector3d getScale (Vector3d scale) {
		return scale.set(getScaleX(), getScaleY(), getScaleZ());
	}

	/** removes the translational part and transposes the matrix. */
	public Matrix4d toNormalMatrix () {
		val[M03] = 0;
		val[M13] = 0;
		val[M23] = 0;
		return inv().tra();
	}

	public String toString () {
		return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "|" + val[M03] + "]\n" //
			+ "[" + val[M10] + "|" + val[M11] + "|" + val[M12] + "|" + val[M13] + "]\n" //
			+ "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "|" + val[M23] + "]\n" //
			+ "[" + val[M30] + "|" + val[M31] + "|" + val[M32] + "|" + val[M33] + "]\n";
	}

	/** Multiplies the matrix mata with matrix matb, storing the result in mata. The arrays are assumed to hold 4x4 column major
	 * matrices as you can get from {@link Matrix4d#val}. This is the same as {@link Matrix4d#mul(Matrix4d)}.
	 *
	 * @param mata the first matrix.
	 * @param matb the second matrix. */
	public static void mul (double[] mata, double[] matb) {
		double m00 = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20] + mata[M03] * matb[M30];
		double m01 = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21] + mata[M03] * matb[M31];
		double m02 = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22] + mata[M03] * matb[M32];
		double m03 = mata[M00] * matb[M03] + mata[M01] * matb[M13] + mata[M02] * matb[M23] + mata[M03] * matb[M33];
		double m10 = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20] + mata[M13] * matb[M30];
		double m11 = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21] + mata[M13] * matb[M31];
		double m12 = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22] + mata[M13] * matb[M32];
		double m13 = mata[M10] * matb[M03] + mata[M11] * matb[M13] + mata[M12] * matb[M23] + mata[M13] * matb[M33];
		double m20 = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20] + mata[M23] * matb[M30];
		double m21 = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21] + mata[M23] * matb[M31];
		double m22 = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22] + mata[M23] * matb[M32];
		double m23 = mata[M20] * matb[M03] + mata[M21] * matb[M13] + mata[M22] * matb[M23] + mata[M23] * matb[M33];
		double m30 = mata[M30] * matb[M00] + mata[M31] * matb[M10] + mata[M32] * matb[M20] + mata[M33] * matb[M30];
		double m31 = mata[M30] * matb[M01] + mata[M31] * matb[M11] + mata[M32] * matb[M21] + mata[M33] * matb[M31];
		double m32 = mata[M30] * matb[M02] + mata[M31] * matb[M12] + mata[M32] * matb[M22] + mata[M33] * matb[M32];
		double m33 = mata[M30] * matb[M03] + mata[M31] * matb[M13] + mata[M32] * matb[M23] + mata[M33] * matb[M33];
		mata[M00] = m00;
		mata[M10] = m10;
		mata[M20] = m20;
		mata[M30] = m30;
		mata[M01] = m01;
		mata[M11] = m11;
		mata[M21] = m21;
		mata[M31] = m31;
		mata[M02] = m02;
		mata[M12] = m12;
		mata[M22] = m22;
		mata[M32] = m32;
		mata[M03] = m03;
		mata[M13] = m13;
		mata[M23] = m23;
		mata[M33] = m33;
	}

	public void mulLeftTo(Matrix4f mat) {
		float m00 = mat.m00 * (float) val[M00] + mat.m01 * (float) val[M10] + mat.m02 * (float) val[M20] + mat.m03 * (float) val[M30];
		float m01 = mat.m00 * (float) val[M01] + mat.m01 * (float) val[M11] + mat.m02 * (float) val[M21] + mat.m03 * (float) val[M31];
		float m02 = mat.m00 * (float) val[M02] + mat.m01 * (float) val[M12] + mat.m02 * (float) val[M22] + mat.m03 * (float) val[M32];
		float m03 = mat.m00 * (float) val[M03] + mat.m01 * (float) val[M13] + mat.m02 * (float) val[M23] + mat.m03 * (float) val[M33];
		float m10 = mat.m10 * (float) val[M00] + mat.m11 * (float) val[M10] + mat.m12 * (float) val[M20] + mat.m13 * (float) val[M30];
		float m11 = mat.m10 * (float) val[M01] + mat.m11 * (float) val[M11] + mat.m12 * (float) val[M21] + mat.m13 * (float) val[M31];
		float m12 = mat.m10 * (float) val[M02] + mat.m11 * (float) val[M12] + mat.m12 * (float) val[M22] + mat.m13 * (float) val[M32];
		float m13 = mat.m10 * (float) val[M03] + mat.m11 * (float) val[M13] + mat.m12 * (float) val[M23] + mat.m13 * (float) val[M33];
		float m20 = mat.m20 * (float) val[M00] + mat.m21 * (float) val[M10] + mat.m22 * (float) val[M20] + mat.m23 * (float) val[M30];
		float m21 = mat.m20 * (float) val[M01] + mat.m21 * (float) val[M11] + mat.m22 * (float) val[M21] + mat.m23 * (float) val[M31];
		float m22 = mat.m20 * (float) val[M02] + mat.m21 * (float) val[M12] + mat.m22 * (float) val[M22] + mat.m23 * (float) val[M32];
		float m23 = mat.m20 * (float) val[M03] + mat.m21 * (float) val[M13] + mat.m22 * (float) val[M23] + mat.m23 * (float) val[M33];
		float m30 = mat.m30 * (float) val[M00] + mat.m31 * (float) val[M10] + mat.m32 * (float) val[M20] + mat.m33 * (float) val[M30];
		float m31 = mat.m30 * (float) val[M01] + mat.m31 * (float) val[M11] + mat.m32 * (float) val[M21] + mat.m33 * (float) val[M31];
		float m32 = mat.m30 * (float) val[M02] + mat.m31 * (float) val[M12] + mat.m32 * (float) val[M22] + mat.m33 * (float) val[M32];
		float m33 = mat.m30 * (float) val[M03] + mat.m31 * (float) val[M13] + mat.m32 * (float) val[M23] + mat.m33 * (float) val[M33];
		mat.m00 = m00;
		mat.m10 = m10;
		mat.m20 = m20;
		mat.m30 = m30;
		mat.m01 = m01;
		mat.m11 = m11;
		mat.m21 = m21;
		mat.m31 = m31;
		mat.m02 = m02;
		mat.m12 = m12;
		mat.m22 = m22;
		mat.m32 = m32;
		mat.m03 = m03;
		mat.m13 = m13;
		mat.m23 = m23;
		mat.m33 = m33;
	}

	/** Multiplies the vector with the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get
	 * from {@link Matrix4d#val}. The vector array is assumed to hold a 3-component vector, with x being the first element, y being
	 * the second and z being the last component. The result is stored in the vector array. This is the same as
	 * {@link Vector3d#mul(Matrix4d)}.
	 * @param mat the matrix
	 * @param vec the vector. */
	public static void mulVec (double[] mat, double[] vec) {
		double x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03];
		double y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13];
		double z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23];
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	/** Multiplies the vector with the given matrix, performing a division by w. The matrix array is assumed to hold a 4x4 column
	 * major matrix as you can get from {@link Matrix4d#val}. The vector array is assumed to hold a 3-component vector, with x being
	 * the first element, y being the second and z being the last component. The result is stored in the vector array. This is the
	 * same as {@link Vector3d#prj(Matrix4d)}.
	 * @param mat the matrix
	 * @param vec the vector. */
	public static void prj (double[] mat, double[] vec) {
		double inv_w = 1.0 / (vec[0] * mat[M30] + vec[1] * mat[M31] + vec[2] * mat[M32] + mat[M33]);
		double x = (vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03]) * inv_w;
		double y = (vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13]) * inv_w;
		double z = (vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23]) * inv_w;
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	/** Multiplies the vector with the top most 3x3 sub-matrix of the given matrix. The matrix array is assumed to hold a 4x4 column
	 * major matrix as you can get from {@link Matrix4d#val}. The vector array is assumed to hold a 3-component vector, with x being
	 * the first element, y being the second and z being the last component. The result is stored in the vector array. This is the
	 * same as {@link Vector3d#rot(Matrix4d)}.
	 * @param mat the matrix
	 * @param vec the vector. */
	public static void rot (double[] mat, double[] vec) {
		double x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02];
		double y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12];
		double z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22];
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	/** Computes the inverse of the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get from
	 * {@link Matrix4d#val}.
	 * @param values the matrix values.
	 * @return false in case the inverse could not be calculated, true otherwise. */
	public static boolean inv (double[] values) {
		double l_det = det(values);
		if (l_det == 0) return false;
		double m00 = values[M12] * values[M23] * values[M31] - values[M13] * values[M22] * values[M31]
			+ values[M13] * values[M21] * values[M32] - values[M11] * values[M23] * values[M32]
			- values[M12] * values[M21] * values[M33] + values[M11] * values[M22] * values[M33];
		double m01 = values[M03] * values[M22] * values[M31] - values[M02] * values[M23] * values[M31]
			- values[M03] * values[M21] * values[M32] + values[M01] * values[M23] * values[M32]
			+ values[M02] * values[M21] * values[M33] - values[M01] * values[M22] * values[M33];
		double m02 = values[M02] * values[M13] * values[M31] - values[M03] * values[M12] * values[M31]
			+ values[M03] * values[M11] * values[M32] - values[M01] * values[M13] * values[M32]
			- values[M02] * values[M11] * values[M33] + values[M01] * values[M12] * values[M33];
		double m03 = values[M03] * values[M12] * values[M21] - values[M02] * values[M13] * values[M21]
			- values[M03] * values[M11] * values[M22] + values[M01] * values[M13] * values[M22]
			+ values[M02] * values[M11] * values[M23] - values[M01] * values[M12] * values[M23];
		double m10 = values[M13] * values[M22] * values[M30] - values[M12] * values[M23] * values[M30]
			- values[M13] * values[M20] * values[M32] + values[M10] * values[M23] * values[M32]
			+ values[M12] * values[M20] * values[M33] - values[M10] * values[M22] * values[M33];
		double m11 = values[M02] * values[M23] * values[M30] - values[M03] * values[M22] * values[M30]
			+ values[M03] * values[M20] * values[M32] - values[M00] * values[M23] * values[M32]
			- values[M02] * values[M20] * values[M33] + values[M00] * values[M22] * values[M33];
		double m12 = values[M03] * values[M12] * values[M30] - values[M02] * values[M13] * values[M30]
			- values[M03] * values[M10] * values[M32] + values[M00] * values[M13] * values[M32]
			+ values[M02] * values[M10] * values[M33] - values[M00] * values[M12] * values[M33];
		double m13 = values[M02] * values[M13] * values[M20] - values[M03] * values[M12] * values[M20]
			+ values[M03] * values[M10] * values[M22] - values[M00] * values[M13] * values[M22]
			- values[M02] * values[M10] * values[M23] + values[M00] * values[M12] * values[M23];
		double m20 = values[M11] * values[M23] * values[M30] - values[M13] * values[M21] * values[M30]
			+ values[M13] * values[M20] * values[M31] - values[M10] * values[M23] * values[M31]
			- values[M11] * values[M20] * values[M33] + values[M10] * values[M21] * values[M33];
		double m21 = values[M03] * values[M21] * values[M30] - values[M01] * values[M23] * values[M30]
			- values[M03] * values[M20] * values[M31] + values[M00] * values[M23] * values[M31]
			+ values[M01] * values[M20] * values[M33] - values[M00] * values[M21] * values[M33];
		double m22 = values[M01] * values[M13] * values[M30] - values[M03] * values[M11] * values[M30]
			+ values[M03] * values[M10] * values[M31] - values[M00] * values[M13] * values[M31]
			- values[M01] * values[M10] * values[M33] + values[M00] * values[M11] * values[M33];
		double m23 = values[M03] * values[M11] * values[M20] - values[M01] * values[M13] * values[M20]
			- values[M03] * values[M10] * values[M21] + values[M00] * values[M13] * values[M21]
			+ values[M01] * values[M10] * values[M23] - values[M00] * values[M11] * values[M23];
		double m30 = values[M12] * values[M21] * values[M30] - values[M11] * values[M22] * values[M30]
			- values[M12] * values[M20] * values[M31] + values[M10] * values[M22] * values[M31]
			+ values[M11] * values[M20] * values[M32] - values[M10] * values[M21] * values[M32];
		double m31 = values[M01] * values[M22] * values[M30] - values[M02] * values[M21] * values[M30]
			+ values[M02] * values[M20] * values[M31] - values[M00] * values[M22] * values[M31]
			- values[M01] * values[M20] * values[M32] + values[M00] * values[M21] * values[M32];
		double m32 = values[M02] * values[M11] * values[M30] - values[M01] * values[M12] * values[M30]
			- values[M02] * values[M10] * values[M31] + values[M00] * values[M12] * values[M31]
			+ values[M01] * values[M10] * values[M32] - values[M00] * values[M11] * values[M32];
		double m33 = values[M01] * values[M12] * values[M20] - values[M02] * values[M11] * values[M20]
			+ values[M02] * values[M10] * values[M21] - values[M00] * values[M12] * values[M21]
			- values[M01] * values[M10] * values[M22] + values[M00] * values[M11] * values[M22];
		double inv_det = 1.0 / l_det;
		values[M00] = m00 * inv_det;
		values[M10] = m10 * inv_det;
		values[M20] = m20 * inv_det;
		values[M30] = m30 * inv_det;
		values[M01] = m01 * inv_det;
		values[M11] = m11 * inv_det;
		values[M21] = m21 * inv_det;
		values[M31] = m31 * inv_det;
		values[M02] = m02 * inv_det;
		values[M12] = m12 * inv_det;
		values[M22] = m22 * inv_det;
		values[M32] = m32 * inv_det;
		values[M03] = m03 * inv_det;
		values[M13] = m13 * inv_det;
		values[M23] = m23 * inv_det;
		values[M33] = m33 * inv_det;
		return true;
	}

	/** Computes the determinante of the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get
	 * from {@link Matrix4d#val}.
	 * @param values the matrix values.
	 * @return the determinante. */
	public static double det (double[] values) {
		return values[M30] * values[M21] * values[M12] * values[M03] - values[M20] * values[M31] * values[M12] * values[M03] - values[M30] * values[M11]
			* values[M22] * values[M03] + values[M10] * values[M31] * values[M22] * values[M03] + values[M20] * values[M11] * values[M32] * values[M03] - values[M10]
			* values[M21] * values[M32] * values[M03] - values[M30] * values[M21] * values[M02] * values[M13] + values[M20] * values[M31] * values[M02] * values[M13]
			+ values[M30] * values[M01] * values[M22] * values[M13] - values[M00] * values[M31] * values[M22] * values[M13] - values[M20] * values[M01] * values[M32]
			* values[M13] + values[M00] * values[M21] * values[M32] * values[M13] + values[M30] * values[M11] * values[M02] * values[M23] - values[M10] * values[M31]
			* values[M02] * values[M23] - values[M30] * values[M01] * values[M12] * values[M23] + values[M00] * values[M31] * values[M12] * values[M23] + values[M10]
			* values[M01] * values[M32] * values[M23] - values[M00] * values[M11] * values[M32] * values[M23] - values[M20] * values[M11] * values[M02] * values[M33]
			+ values[M10] * values[M21] * values[M02] * values[M33] + values[M20] * values[M01] * values[M12] * values[M33] - values[M00] * values[M21] * values[M12]
			* values[M33] - values[M10] * values[M01] * values[M22] * values[M33] + values[M00] * values[M11] * values[M22] * values[M33];
		
	}

	/** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES'
	 * glTranslate/glRotate/glScale
	 * @param translation
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d translate (Vector3d translation) {
		return translate(translation.x, translation.y, translation.z);
	}

	/** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param x Translation in the x-axis.
	 * @param y Translation in the y-axis.
	 * @param z Translation in the z-axis.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d translate (double x, double y, double z) {
		val[M03] += val[M00] * x + val[M01] * y + val[M02] * z;
		val[M13] += val[M10] * x + val[M11] * y + val[M12] * z;
		val[M23] += val[M20] * x + val[M21] * y + val[M22] * z;
		val[M33] += val[M30] * x + val[M31] * y + val[M32] * z;
		return this;
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param axis The vector axis to rotate around.
	 * @param degrees The angle in degrees.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d rotate (Vector3d axis, double degrees) {
		if (degrees == 0) return this;
		quat.set(axis, degrees);
		return rotate(quat);
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param axis The vector axis to rotate around.
	 * @param radians The angle in radians.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d rotateRad (Vector3d axis, double radians) {
		if (radians == 0) return this;
		quat.setFromAxisRad(axis, radians);
		return rotate(quat);
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale
	 * @param axisX The x-axis component of the vector to rotate around.
	 * @param axisY The y-axis component of the vector to rotate around.
	 * @param axisZ The z-axis component of the vector to rotate around.
	 * @param degrees The angle in degrees
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d rotate (double axisX, double axisY, double axisZ, double degrees) {
		if (degrees == 0) return this;
		quat.setFromAxis(axisX, axisY, axisZ, degrees);
		return rotate(quat);
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale
	 * @param axisX The x-axis component of the vector to rotate around.
	 * @param axisY The y-axis component of the vector to rotate around.
	 * @param axisZ The z-axis component of the vector to rotate around.
	 * @param radians The angle in radians
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d rotateRad (double axisX, double axisY, double axisZ, double radians) {
		if (radians == 0) return this;
		quat.setFromAxisRad(axisX, axisY, axisZ, radians);
		return rotate(quat);
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param rotation
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d rotate (Quat4d rotation) {
		double x = rotation.x, y = rotation.y, z = rotation.z, w = rotation.w;
		double xx = x * x;
		double xy = x * y;
		double xz = x * z;
		double xw = x * w;
		double yy = y * y;
		double yz = y * z;
		double yw = y * w;
		double zz = z * z;
		double zw = z * w;
		// Set matrix from quaternion
		double r00 = 1 - 2 * (yy + zz);
		double r01 = 2 * (xy - zw);
		double r02 = 2 * (xz + yw);
		double r10 = 2 * (xy + zw);
		double r11 = 1 - 2 * (xx + zz);
		double r12 = 2 * (yz - xw);
		double r20 = 2 * (xz - yw);
		double r21 = 2 * (yz + xw);
		double r22 = 1 - 2 * (xx + yy);
		double m00 = val[M00] * r00 + val[M01] * r10 + val[M02] * r20;
		double m01 = val[M00] * r01 + val[M01] * r11 + val[M02] * r21;
		double m02 = val[M00] * r02 + val[M01] * r12 + val[M02] * r22;
		double m10 = val[M10] * r00 + val[M11] * r10 + val[M12] * r20;
		double m11 = val[M10] * r01 + val[M11] * r11 + val[M12] * r21;
		double m12 = val[M10] * r02 + val[M11] * r12 + val[M12] * r22;
		double m20 = val[M20] * r00 + val[M21] * r10 + val[M22] * r20;
		double m21 = val[M20] * r01 + val[M21] * r11 + val[M22] * r21;
		double m22 = val[M20] * r02 + val[M21] * r12 + val[M22] * r22;
		double m30 = val[M30] * r00 + val[M31] * r10 + val[M32] * r20;
		double m31 = val[M30] * r01 + val[M31] * r11 + val[M32] * r21;
		double m32 = val[M30] * r02 + val[M31] * r12 + val[M32] * r22;
		val[M00] = m00;
		val[M10] = m10;
		val[M20] = m20;
		val[M30] = m30;
		val[M01] = m01;
		val[M11] = m11;
		val[M21] = m21;
		val[M31] = m31;
		val[M02] = m02;
		val[M12] = m12;
		val[M22] = m22;
		val[M32] = m32;
		return this;
	}

	/** Postmultiplies this matrix by the rotation between two vectors.
	 * @param v1 The base vector
	 * @param v2 The target vector
	 * @return This matrix for the purpose of chaining methods together */
	public Matrix4d rotate (final Vector3d v1, final Vector3d v2) {
		return rotate(quat.setFromCross(v1, v2));
	}

	/** Post-multiplies this matrix by a rotation toward a direction.
	 * @param direction direction to rotate toward
	 * @param up up vector
	 * @return This matrix for chaining */
	public Matrix4d rotateTowardDirection (final Vector3d direction, final Vector3d up) {
		l_vez.set(direction).nor();
		l_vex.set(direction).crs(up).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
		double m00 = val[M00] * l_vex.x + val[M01] * l_vex.y + val[M02] * l_vex.z;
		double m01 = val[M00] * l_vey.x + val[M01] * l_vey.y + val[M02] * l_vey.z;
		double m02 = val[M00] * -l_vez.x + val[M01] * -l_vez.y + val[M02] * -l_vez.z;
		double m10 = val[M10] * l_vex.x + val[M11] * l_vex.y + val[M12] * l_vex.z;
		double m11 = val[M10] * l_vey.x + val[M11] * l_vey.y + val[M12] * l_vey.z;
		double m12 = val[M10] * -l_vez.x + val[M11] * -l_vez.y + val[M12] * -l_vez.z;
		double m20 = val[M20] * l_vex.x + val[M21] * l_vex.y + val[M22] * l_vex.z;
		double m21 = val[M20] * l_vey.x + val[M21] * l_vey.y + val[M22] * l_vey.z;
		double m22 = val[M20] * -l_vez.x + val[M21] * -l_vez.y + val[M22] * -l_vez.z;
		double m30 = val[M30] * l_vex.x + val[M31] * l_vex.y + val[M32] * l_vex.z;
		double m31 = val[M30] * l_vey.x + val[M31] * l_vey.y + val[M32] * l_vey.z;
		double m32 = val[M30] * -l_vez.x + val[M31] * -l_vez.y + val[M32] * -l_vez.z;
		val[M00] = m00;
		val[M10] = m10;
		val[M20] = m20;
		val[M30] = m30;
		val[M01] = m01;
		val[M11] = m11;
		val[M21] = m21;
		val[M31] = m31;
		val[M02] = m02;
		val[M12] = m12;
		val[M22] = m22;
		val[M32] = m32;
		return this;
	}

	/** Post-multiplies this matrix by a rotation toward a target.
	 * @param target the target to rotate to
	 * @param up the up vector
	 * @return This matrix for chaining */
	public Matrix4d rotateTowardTarget (final Vector3d target, final Vector3d up) {
		tmpVec.set(target.x - val[M03], target.y - val[M13], target.z - val[M23]);
		return rotateTowardDirection(tmpVec, up);
	}

	/** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param scaleX The scale in the x-axis.
	 * @param scaleY The scale in the y-axis.
	 * @param scaleZ The scale in the z-axis.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix4d scale (double scaleX, double scaleY, double scaleZ) {
		val[M00] *= scaleX;
		val[M01] *= scaleY;
		val[M02] *= scaleZ;
		val[M10] *= scaleX;
		val[M11] *= scaleY;
		val[M12] *= scaleZ;
		val[M20] *= scaleX;
		val[M21] *= scaleY;
		val[M22] *= scaleZ;
		val[M30] *= scaleX;
		val[M31] *= scaleY;
		val[M32] *= scaleZ;
		return this;
	}

	public Matrix4d scale (final Vector3d scl) {
		return scale(scl.x, scl.y, scl.z);
	}

	/** Copies the 4x3 upper-left sub-matrix into double array. The destination array is supposed to be a column major matrix.
	 * @param dst the destination matrix */
	public void extract4x3Matrix (double[] dst) {
		dst[0] = val[M00];
		dst[1] = val[M10];
		dst[2] = val[M20];
		dst[3] = val[M01];
		dst[4] = val[M11];
		dst[5] = val[M21];
		dst[6] = val[M02];
		dst[7] = val[M12];
		dst[8] = val[M22];
		dst[9] = val[M03];
		dst[10] = val[M13];
		dst[11] = val[M23];
	}

	/** @return True if this matrix has any rotation or scaling, false otherwise */
	public boolean hasRotationOrScaling () {
		return !(Math.abs(val[M00] - 1) < 1e-5 && Math.abs(val[M11] - 1) < 1e-5 && Math.abs(val[M22] - 1) < 1e-5
			&& Math.abs(val[M01]) < 1e-5 && Math.abs(val[M02]) < 1e-5 && Math.abs(val[M10]) < 1e-5 && Math.abs(val[M12]) < 1e-5
			&& Math.abs(val[M20]) < 1e-5 && Math.abs(val[M21]) < 1e-5);
	}
}
