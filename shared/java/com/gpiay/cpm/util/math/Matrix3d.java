package com.gpiay.cpm.util.math;

import net.minecraft.util.math.vector.Matrix3f;

/** A 3x3 <a href="http://en.wikipedia.org/wiki/Row-major_order#Column-major_order">column major</a> matrix; useful for 2D
 * transforms.
 * 
 * @author mzechner */
public class Matrix3d {
	public static final int M00 = 0;
	public static final int M01 = 3;
	public static final int M02 = 6;
	public static final int M10 = 1;
	public static final int M11 = 4;
	public static final int M12 = 7;
	public static final int M20 = 2;
	public static final int M21 = 5;
	public static final int M22 = 8;
	public double[] val = new double[9];
	private double[] tmp = new double[9];

	public Matrix3d() {
		idt();
	}

	public Matrix3d(Matrix3d matrix) {
		set(matrix);
	}

	/** Constructs a matrix from the given double array. The array must have at least 9 elements; the first 9 will be copied.
	 * @param values The double array to copy. Remember that this matrix is in <a
	 *           href="http://en.wikipedia.org/wiki/Row-major_order#Column-major_order">column major</a> order. (The double array is
	 *           not modified.) */
	public Matrix3d(double[] values) {
		this.set(values);
	}

	/** Sets this matrix to the identity matrix
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d idt () {
		double[] val = this.val;
		val[M00] = 1;
		val[M10] = 0;
		val[M20] = 0;
		val[M01] = 0;
		val[M11] = 1;
		val[M21] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;
		return this;
	}

	/** Postmultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
	 * 
	 * <pre>
	 * A.mul(B) results in A := AB
	 * </pre>
	 * @param m Matrix to multiply by.
	 * @return This matrix for the purpose of chaining operations together. */
	public Matrix3d mul (Matrix3d m) {
		double[] val = this.val;

		double v00 = val[M00] * m.val[M00] + val[M01] * m.val[M10] + val[M02] * m.val[M20];
		double v01 = val[M00] * m.val[M01] + val[M01] * m.val[M11] + val[M02] * m.val[M21];
		double v02 = val[M00] * m.val[M02] + val[M01] * m.val[M12] + val[M02] * m.val[M22];

		double v10 = val[M10] * m.val[M00] + val[M11] * m.val[M10] + val[M12] * m.val[M20];
		double v11 = val[M10] * m.val[M01] + val[M11] * m.val[M11] + val[M12] * m.val[M21];
		double v12 = val[M10] * m.val[M02] + val[M11] * m.val[M12] + val[M12] * m.val[M22];

		double v20 = val[M20] * m.val[M00] + val[M21] * m.val[M10] + val[M22] * m.val[M20];
		double v21 = val[M20] * m.val[M01] + val[M21] * m.val[M11] + val[M22] * m.val[M21];
		double v22 = val[M20] * m.val[M02] + val[M21] * m.val[M12] + val[M22] * m.val[M22];

		val[M00] = v00;
		val[M10] = v10;
		val[M20] = v20;
		val[M01] = v01;
		val[M11] = v11;
		val[M21] = v21;
		val[M02] = v02;
		val[M12] = v12;
		val[M22] = v22;

		return this;
	}

	/** Premultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
	 * 
	 * <pre>
	 * A.mulLeft(B) results in A := BA
	 * </pre>
	 * @param m The other Matrix to multiply by
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d mulLeft (Matrix3d m) {
		double[] val = this.val;

		double v00 = m.val[M00] * val[M00] + m.val[M01] * val[M10] + m.val[M02] * val[M20];
		double v01 = m.val[M00] * val[M01] + m.val[M01] * val[M11] + m.val[M02] * val[M21];
		double v02 = m.val[M00] * val[M02] + m.val[M01] * val[M12] + m.val[M02] * val[M22];

		double v10 = m.val[M10] * val[M00] + m.val[M11] * val[M10] + m.val[M12] * val[M20];
		double v11 = m.val[M10] * val[M01] + m.val[M11] * val[M11] + m.val[M12] * val[M21];
		double v12 = m.val[M10] * val[M02] + m.val[M11] * val[M12] + m.val[M12] * val[M22];

		double v20 = m.val[M20] * val[M00] + m.val[M21] * val[M10] + m.val[M22] * val[M20];
		double v21 = m.val[M20] * val[M01] + m.val[M21] * val[M11] + m.val[M22] * val[M21];
		double v22 = m.val[M20] * val[M02] + m.val[M21] * val[M12] + m.val[M22] * val[M22];

		val[M00] = v00;
		val[M10] = v10;
		val[M20] = v20;
		val[M01] = v01;
		val[M11] = v11;
		val[M21] = v21;
		val[M02] = v02;
		val[M12] = v12;
		val[M22] = v22;

		return this;
	}

	/** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
	 * @param degrees the angle in degrees.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToRotation (double degrees) {
		return setToRotationRad(Quat4d.degreesToRadians * degrees);
	}

	/** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
	 * @param radians the angle in radians.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToRotationRad (double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		double[] val = this.val;

		val[M00] = cos;
		val[M10] = sin;
		val[M20] = 0;

		val[M01] = -sin;
		val[M11] = cos;
		val[M21] = 0;

		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;

		return this;
	}

	public Matrix3d setToRotation (Vector3d axis, double degrees) {
		return setToRotation(axis, Math.cos(Quat4d.degreesToRadians * degrees), Math.sin(Quat4d.degreesToRadians * degrees));
	}

	public Matrix3d setToRotation (Vector3d axis, double cos, double sin) {
		double[] val = this.val;
		double oc = 1.0 - cos;
		val[M00] = oc * axis.x * axis.x + cos;
		val[M01] = oc * axis.x * axis.y - axis.z * sin;
		val[M02] = oc * axis.z * axis.x + axis.y * sin;
		val[M10] = oc * axis.x * axis.y + axis.z * sin;
		val[M11] = oc * axis.y * axis.y + cos;
		val[M12] = oc * axis.y * axis.z - axis.x * sin;
		val[M20] = oc * axis.z * axis.x - axis.y * sin;
		val[M21] = oc * axis.y * axis.z + axis.x * sin;
		val[M22] = oc * axis.z * axis.z + cos;
		return this;
	}

	/** Sets this matrix to a translation matrix.
	 * @param x the translation in x
	 * @param y the translation in y
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToTranslation (double x, double y) {
		double[] val = this.val;

		val[M00] = 1;
		val[M10] = 0;
		val[M20] = 0;

		val[M01] = 0;
		val[M11] = 1;
		val[M21] = 0;

		val[M02] = x;
		val[M12] = y;
		val[M22] = 1;

		return this;
	}

	/** Sets this matrix to a translation matrix.
	 * @param translation The translation vector.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToTranslation (Vector2d translation) {
		double[] val = this.val;

		val[M00] = 1;
		val[M10] = 0;
		val[M20] = 0;

		val[M01] = 0;
		val[M11] = 1;
		val[M21] = 0;

		val[M02] = translation.x;
		val[M12] = translation.y;
		val[M22] = 1;

		return this;
	}

	/** Sets this matrix to a scaling matrix.
	 * 
	 * @param scaleX the scale in x
	 * @param scaleY the scale in y
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToScaling (double scaleX, double scaleY) {
		double[] val = this.val;
		val[M00] = scaleX;
		val[M10] = 0;
		val[M20] = 0;
		val[M01] = 0;
		val[M11] = scaleY;
		val[M21] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;
		return this;
	}

	/** Sets this matrix to a scaling matrix.
	 * @param scale The scale vector.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d setToScaling (Vector2d scale) {
		double[] val = this.val;
		val[M00] = scale.x;
		val[M10] = 0;
		val[M20] = 0;
		val[M01] = 0;
		val[M11] = scale.y;
		val[M21] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;
		return this;
	}

	public String toString () {
		double[] val = this.val;
		return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "]\n" //
			+ "[" + val[M10] + "|" + val[M11] + "|" + val[M12] + "]\n" //
			+ "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "]";
	}

	/** @return The determinant of this matrix */
	public double det () {
		double[] val = this.val;
		return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00]
			* val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
	}

	/** Inverts this matrix given that the determinant is != 0.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d inv () {
		double det = det();

		double inv_det = 1.0 / det;
		double[] tmp = this.tmp, val = this.val;

		tmp[M00] = val[M11] * val[M22] - val[M21] * val[M12];
		tmp[M10] = val[M20] * val[M12] - val[M10] * val[M22];
		tmp[M20] = val[M10] * val[M21] - val[M20] * val[M11];
		tmp[M01] = val[M21] * val[M02] - val[M01] * val[M22];
		tmp[M11] = val[M00] * val[M22] - val[M20] * val[M02];
		tmp[M21] = val[M20] * val[M01] - val[M00] * val[M21];
		tmp[M02] = val[M01] * val[M12] - val[M11] * val[M02];
		tmp[M12] = val[M10] * val[M02] - val[M00] * val[M12];
		tmp[M22] = val[M00] * val[M11] - val[M10] * val[M01];

		val[M00] = inv_det * tmp[M00];
		val[M10] = inv_det * tmp[M10];
		val[M20] = inv_det * tmp[M20];
		val[M01] = inv_det * tmp[M01];
		val[M11] = inv_det * tmp[M11];
		val[M21] = inv_det * tmp[M21];
		val[M02] = inv_det * tmp[M02];
		val[M12] = inv_det * tmp[M12];
		val[M22] = inv_det * tmp[M22];

		return this;
	}

	/** Copies the values from the provided matrix to this matrix.
	 * @param mat The matrix to copy.
	 * @return This matrix for the purposes of chaining. */
	public Matrix3d set (Matrix3d mat) {
		System.arraycopy(mat.val, 0, val, 0, val.length);
		return this;
	}

	/** Sets this 3x3 matrix to the top left 3x3 corner of the provided 4x4 matrix.
	 * @param mat The matrix whose top left corner will be copied. This matrix will not be modified.
	 * @return This matrix for the purpose of chaining operations. */
	public Matrix3d set (Matrix4d mat) {
		double[] val = this.val;
		val[M00] = mat.val[Matrix4d.M00];
		val[M10] = mat.val[Matrix4d.M10];
		val[M20] = mat.val[Matrix4d.M20];
		val[M01] = mat.val[Matrix4d.M01];
		val[M11] = mat.val[Matrix4d.M11];
		val[M21] = mat.val[Matrix4d.M21];
		val[M02] = mat.val[Matrix4d.M02];
		val[M12] = mat.val[Matrix4d.M12];
		val[M22] = mat.val[Matrix4d.M22];
		return this;
	}

	/** Sets the matrix to the given matrix as a double array. The double array must have at least 9 elements; the first 9 will be
	 * copied.
	 * 
	 * @param values The matrix, in double form, that is to be copied. Remember that this matrix is in <a
	 *           href="http://en.wikipedia.org/wiki/Row-major_order#Column-major_order">column major</a> order.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix3d set (double[] values) {
		System.arraycopy(values, 0, val, 0, val.length);
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param vector The translation vector.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d trn (Vector2d vector) {
		val[M02] += vector.x;
		val[M12] += vector.y;
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d trn (double x, double y) {
		val[M02] += x;
		val[M12] += y;
		return this;
	}

	/** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 * @param vector The translation vector. (The z-component of the vector is ignored because this is a 3x3 matrix)
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d trn (Vector3d vector) {
		val[M02] += vector.x;
		val[M12] += vector.y;
		return this;
	}

	/** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d translate (double x, double y) {
		double[] val = this.val;
		tmp[M00] = 1;
		tmp[M10] = 0;
		tmp[M20] = 0;

		tmp[M01] = 0;
		tmp[M11] = 1;
		tmp[M21] = 0;

		tmp[M02] = x;
		tmp[M12] = y;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param translation The translation vector.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d translate (Vector2d translation) {
		double[] val = this.val;
		tmp[M00] = 1;
		tmp[M10] = 0;
		tmp[M20] = 0;

		tmp[M01] = 0;
		tmp[M11] = 1;
		tmp[M21] = 0;

		tmp[M02] = translation.x;
		tmp[M12] = translation.y;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param degrees The angle in degrees
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d rotate (double degrees) {
		return rotateRad(Quat4d.degreesToRadians * degrees);
	}

	/** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param radians The angle in radians
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d rotateRad (double radians) {
		if (radians == 0) return this;
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		double[] tmp = this.tmp;

		tmp[M00] = cos;
		tmp[M10] = sin;
		tmp[M20] = 0;

		tmp[M01] = -sin;
		tmp[M11] = cos;
		tmp[M21] = 0;

		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param scaleX The scale in the x-axis.
	 * @param scaleY The scale in the y-axis.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d scale (double scaleX, double scaleY) {
		double[] tmp = this.tmp;
		tmp[M00] = scaleX;
		tmp[M10] = 0;
		tmp[M20] = 0;
		tmp[M01] = 0;
		tmp[M11] = scaleY;
		tmp[M21] = 0;
		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 * @param scale The vector to scale the matrix by.
	 * @return This matrix for the purpose of chaining. */
	public Matrix3d scale (Vector2d scale) {
		double[] tmp = this.tmp;
		tmp[M00] = scale.x;
		tmp[M10] = 0;
		tmp[M20] = 0;
		tmp[M01] = 0;
		tmp[M11] = scale.y;
		tmp[M21] = 0;
		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/** Get the values in this matrix.
	 * @return The double values that make up this matrix in column-major order. */
	public double[] getValues () {
		return val;
	}

	public Vector2d getTranslation (Vector2d position) {
		position.x = val[M02];
		position.y = val[M12];
		return position;
	}
	/** @param scale The vector which will receive the (non-negative) scale components on each axis.
	 * @return The provided vector for chaining. */
	public Vector2d getScale (Vector2d scale) {
		double[] val = this.val;
		scale.x = Math.sqrt(val[M00] * val[M00] + val[M01] * val[M01]);
		scale.y = Math.sqrt(val[M10] * val[M10] + val[M11] * val[M11]);
		return scale;
	}

	public double getRotation () {
		return Quat4d.radiansToDegrees * Math.atan2(val[M10], val[M00]);
	}

	public double getRotationRad () {
		return Math.atan2(val[M10], val[M00]);
	}

	/** Scale the matrix in the both the x and y components by the scalar value.
	 * @param scale The single value that will be used to scale both the x and y components.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix3d scl (double scale) {
		val[M00] *= scale;
		val[M11] *= scale;
		return this;
	}

	/** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
	 * @param scale The {@link Vector3d} to use to scale this matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix3d scl (Vector2d scale) {
		val[M00] *= scale.x;
		val[M11] *= scale.y;
		return this;
	}

	/** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
	 * @param scale The {@link Vector3d} to use to scale this matrix. The z component will be ignored.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix3d scl (Vector3d scale) {
		val[M00] *= scale.x;
		val[M11] *= scale.y;
		return this;
	}

	/** Transposes the current matrix.
	 * @return This matrix for the purpose of chaining methods together. */
	public Matrix3d transpose () {
		// Where MXY you do not have to change MXX
		double[] val = this.val;
		double v01 = val[M10];
		double v02 = val[M20];
		double v10 = val[M01];
		double v12 = val[M21];
		double v20 = val[M02];
		double v21 = val[M12];
		val[M01] = v01;
		val[M02] = v02;
		val[M10] = v10;
		val[M12] = v12;
		val[M20] = v20;
		val[M21] = v21;
		return this;
	}

	/** Multiplies matrix a with matrix b in the following manner:
	 * 
	 * <pre>
	 * mul(A, B) => A := AB
	 * </pre>
	 * @param mata The double array representing the first matrix. Must have at least 9 elements.
	 * @param matb The double array representing the second matrix. Must have at least 9 elements. */
	private static void mul (double[] mata, double[] matb) {
		double v00 = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20];
		double v01 = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21];
		double v02 = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22];

		double v10 = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20];
		double v11 = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21];
		double v12 = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22];

		double v20 = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20];
		double v21 = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21];
		double v22 = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22];

		mata[M00] = v00;
		mata[M10] = v10;
		mata[M20] = v20;
		mata[M01] = v01;
		mata[M11] = v11;
		mata[M21] = v21;
		mata[M02] = v02;
		mata[M12] = v12;
		mata[M22] = v22;
	}

	public void mulLeftTo(Matrix3f mat) {
		float v00 = mat.m00 * (float) val[M00] + mat.m01 * (float) val[M10] + mat.m02 * (float) val[M20];
		float v01 = mat.m00 * (float) val[M01] + mat.m01 * (float) val[M11] + mat.m02 * (float) val[M21];
		float v02 = mat.m00 * (float) val[M02] + mat.m01 * (float) val[M12] + mat.m02 * (float) val[M22];

		float v10 = mat.m10 * (float) val[M00] + mat.m11 * (float) val[M10] + mat.m12 * (float) val[M20];
		float v11 = mat.m10 * (float) val[M01] + mat.m11 * (float) val[M11] + mat.m12 * (float) val[M21];
		float v12 = mat.m10 * (float) val[M02] + mat.m11 * (float) val[M12] + mat.m12 * (float) val[M22];

		float v20 = mat.m20 * (float) val[M00] + mat.m21 * (float) val[M10] + mat.m22 * (float) val[M20];
		float v21 = mat.m20 * (float) val[M01] + mat.m21 * (float) val[M11] + mat.m22 * (float) val[M21];
		float v22 = mat.m20 * (float) val[M02] + mat.m21 * (float) val[M12] + mat.m22 * (float) val[M22];

		mat.m00 = v00;
		mat.m10 = v10;
		mat.m20 = v20;
		mat.m01 = v01;
		mat.m11 = v11;
		mat.m21 = v21;
		mat.m02 = v02;
		mat.m12 = v12;
		mat.m22 = v22;
	}
}
