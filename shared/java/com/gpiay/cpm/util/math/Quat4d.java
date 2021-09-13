package com.gpiay.cpm.util.math;

import net.minecraft.util.math.MathHelper;

/** A simple quaternion class.
 * @see <a href="http://en.wikipedia.org/wiki/Quaternion">http://en.wikipedia.org/wiki/Quaternion</a>
 * @author badlogicgames@gmail.com
 * @author vesuvio
 * @author xoppa */
public class Quat4d {
	private static Quat4d tmp1 = new Quat4d(0, 0, 0, 0);
	private static Quat4d tmp2 = new Quat4d(0, 0, 0, 0);

	public static final Quat4d Zero = new Quat4d(0, 0, 0, 0);
	public static final Quat4d One = new Quat4d(1, 1, 1, 1);

	public final static double degreesToRadians = Math.PI / 180;
	public final static double radiansToDegrees = 180 / Math.PI;
	public final static double PI2 = Math.PI * 2;

	public double x;
	public double y;
	public double z;
	public double w;

	/** Constructor, sets the four components of the quaternion.
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component */
	public Quat4d(double x, double y, double z, double w) {
		this.set(x, y, z, w);
	}

	public Quat4d() {
		idt();
	}

	/** Constructor, sets the quaternion components from the given quaternion.
	 * 
	 * @param quat4d The quaternion to copy. */
	public Quat4d(Quat4d quat4d) {
		this.set(quat4d);
	}

	/** Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees. */
	public Quat4d(Vector3d axis, double angle) {
		this.set(axis, angle);
	}

	/** Sets the components of the quaternion
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component
	 * @return This quaternion for chaining */
	public Quat4d set (double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/** Sets the quaternion components from the given quaternion.
	 * @param quat4d The quaternion.
	 * @return This quaternion for chaining. */
	public Quat4d set (Quat4d quat4d) {
		return this.set(quat4d.x, quat4d.y, quat4d.z, quat4d.w);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining. */
	public Quat4d set (Vector3d axis, double angle) {
		return setFromAxis(axis.x, axis.y, axis.z, angle);
	}

	/** @return a copy of this quaternion */
	public Quat4d cpy () {
		return new Quat4d(this);
	}

	/** @return the euclidean length of the specified quaternion */
	public final static double len (final double x, final double y, final double z, final double w) {
		return Math.sqrt(x * x + y * y + z * z + w * w);
	}

	/** @return the euclidean length of this quaternion */
	public double len () {
		return Math.sqrt(x * x + y * y + z * z + w * w);
	}

	@Override
	public String toString () {
		return "[" + x + "|" + y + "|" + z + "|" + w + "]";
	}

	/** Sets the quaternion to the given euler angles in degrees.
	 * @param yaw the rotation around the y axis in degrees
	 * @param pitch the rotation around the x axis in degrees
	 * @param roll the rotation around the z axis degrees
	 * @return this quaternion */
	public Quat4d setEulerAngles (double yaw, double pitch, double roll) {
		return setEulerAnglesRad(yaw * degreesToRadians, pitch * degreesToRadians, roll
			* degreesToRadians);
	}

	/** Sets the quaternion to the given euler angles in radians.
	 * @param yaw the rotation around the y axis in radians
	 * @param pitch the rotation around the x axis in radians
	 * @param roll the rotation around the z axis in radians
	 * @return this quaternion */
	public Quat4d setEulerAnglesRad (double yaw, double pitch, double roll) {
		final double hr = roll * 0.5;
		final double shr = Math.sin(hr);
		final double chr = Math.cos(hr);
		final double hp = pitch * 0.5;
		final double shp = Math.sin(hp);
		final double chp = Math.cos(hp);
		final double hy = yaw * 0.5;
		final double shy = Math.sin(hy);
		final double chy = Math.cos(hy);
		final double chy_shp = chy * shp;
		final double shy_chp = shy * chp;
		final double chy_chp = chy * chp;
		final double shy_shp = shy * shp;

		x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
		y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
		z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
		w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
		return this;
	}

	/** Get the pole of the gimbal lock, if any.
	 * @return positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock */
	public int getGimbalPole () {
		final double t = y * x + z * w;
		return t > 0.499 ? 1 : (t < -0.499 ? -1 : 0);
	}

	/** Get the roll euler angle in radians, which is the rotation around the z axis. Requires that this quaternion is normalized.
	 * @return the rotation around the z axis in radians (between -PI and +PI) */
	public double getRollRad () {
		final int pole = getGimbalPole();
		return pole == 0 ? MathHelper.atan2(2 * (w * z + y * x), 1 - 2 * (x * x + z * z)) : pole * 2
			* MathHelper.atan2(y, w);
	}

	/** Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
	 * @return the rotation around the z axis in degrees (between -180 and +180) */
	public double getRoll () {
		return getRollRad() * radiansToDegrees;
	}

	/** Get the pitch euler angle in radians, which is the rotation around the x axis. Requires that this quaternion is normalized.
	 * @return the rotation around the x axis in radians (between -(PI/2) and +(PI/2)) */
	public double getPitchRad () {
		final int pole = getGimbalPole();
		return pole == 0 ? Math.asin(MathHelper.clamp(2 * (w * x - z * y), -1, 1)) : pole * Math.PI * 0.5;
	}

	/** Get the pitch euler angle in degrees, which is the rotation around the x axis. Requires that this quaternion is normalized.
	 * @return the rotation around the x axis in degrees (between -90 and +90) */
	public double getPitch () {
		return getPitchRad() * radiansToDegrees;
	}

	/** Get the yaw euler angle in radians, which is the rotation around the y axis. Requires that this quaternion is normalized.
	 * @return the rotation around the y axis in radians (between -PI and +PI) */
	public double getYawRad () {
		return getGimbalPole() == 0 ? MathHelper.atan2(2 * (y * w + x * z), 1 - 2 * (y * y + x * x)) : 0;
	}

	/** Get the yaw euler angle in degrees, which is the rotation around the y axis. Requires that this quaternion is normalized.
	 * @return the rotation around the y axis in degrees (between -180 and +180) */
	public double getYaw () {
		return getYawRad() * radiansToDegrees;
	}

	public final static double len2 (final double x, final double y, final double z, final double w) {
		return x * x + y * y + z * z + w * w;
	}

	/** @return the length of this quaternion without square root */
	public double len2 () {
		return x * x + y * y + z * z + w * w;
	}

	/** Normalizes this quaternion to unit length
	 * @return the quaternion for chaining */
	public Quat4d nor () {
		double len = len2();
		if (len != 0. && !(Math.abs(len - 1) < 1e-5)) {
			len = Math.sqrt(len);
			w /= len;
			x /= len;
			y /= len;
			z /= len;
		}
		return this;
	}

	/** Conjugate the quaternion.
	 * 
	 * @return This quaternion for chaining */
	public Quat4d conjugate () {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	// TODO : this would better fit into the vector3 class
	/** Transforms the given vector using this quaternion
	 * 
	 * @param v Vector to transform */
	public Vector3d transform (Vector3d v) {
		tmp2.set(this);
		tmp2.conjugate();
		tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0)).mulLeft(this);

		v.x = tmp2.x;
		v.y = tmp2.y;
		v.z = tmp2.z;
		return v;
	}

	/** Multiplies this quaternion with another one in the form of this = this * other
	 * 
	 * @param other Quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quat4d mul (final Quat4d other) {
		final double newX = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
		final double newY = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
		final double newZ = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
		final double newW = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = this * other
	 * 
	 * @param x the x component of the other quaternion to multiply with
	 * @param y the y component of the other quaternion to multiply with
	 * @param z the z component of the other quaternion to multiply with
	 * @param w the w component of the other quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quat4d mul (final double x, final double y, final double z, final double w) {
		final double newX = this.w * x + this.x * w + this.y * z - this.z * y;
		final double newY = this.w * y + this.y * w + this.z * x - this.x * z;
		final double newZ = this.w * z + this.z * w + this.x * y - this.y * x;
		final double newW = this.w * w - this.x * x - this.y * y - this.z * z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = other * this
	 * 
	 * @param other Quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quat4d mulLeft (Quat4d other) {
		final double newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y;
		final double newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
		final double newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
		final double newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of this = other * this
	 * 
	 * @param x the x component of the other quaternion to multiply with
	 * @param y the y component of the other quaternion to multiply with
	 * @param z the z component of the other quaternion to multiply with
	 * @param w the w component of the other quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quat4d mulLeft (final double x, final double y, final double z, final double w) {
		final double newX = w * this.x + x * this.w + y * this.z - z * this.y;
		final double newY = w * this.y + y * this.w + z * this.x - x * this.z;
		final double newZ = w * this.z + z * this.w + x * this.y - y * this.x;
		final double newW = w * this.w - x * this.x - y * this.y - z * this.z;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		this.w = newW;
		return this;
	}

	/** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
	public Quat4d add (Quat4d quat4d) {
		this.x += quat4d.x;
		this.y += quat4d.y;
		this.z += quat4d.z;
		this.w += quat4d.w;
		return this;
	}

	/** Add the x,y,z,w components of the passed in quaternion to the ones of this quaternion */
	public Quat4d add (double qx, double qy, double qz, double qw) {
		this.x += qx;
		this.y += qy;
		this.z += qz;
		this.w += qw;
		return this;
	}

	// TODO : the matrix4 set(quaternion) doesnt set the last row+col of the matrix to 0,0,0,1 so... that's why there is this
// method
	/** Fills a 4x4 matrix with the rotation matrix represented by this quaternion.
	 * 
	 * @param matrix Matrix to fill */
	public void toMatrix (final double[] matrix) {
		final double xx = x * x;
		final double xy = x * y;
		final double xz = x * z;
		final double xw = x * w;
		final double yy = y * y;
		final double yz = y * z;
		final double yw = y * w;
		final double zz = z * z;
		final double zw = z * w;
		// Set matrix from quaternion
		matrix[Matrix4d.M00] = 1 - 2 * (yy + zz);
		matrix[Matrix4d.M01] = 2 * (xy - zw);
		matrix[Matrix4d.M02] = 2 * (xz + yw);
		matrix[Matrix4d.M03] = 0;
		matrix[Matrix4d.M10] = 2 * (xy + zw);
		matrix[Matrix4d.M11] = 1 - 2 * (xx + zz);
		matrix[Matrix4d.M12] = 2 * (yz - xw);
		matrix[Matrix4d.M13] = 0;
		matrix[Matrix4d.M20] = 2 * (xz - yw);
		matrix[Matrix4d.M21] = 2 * (yz + xw);
		matrix[Matrix4d.M22] = 1 - 2 * (xx + yy);
		matrix[Matrix4d.M23] = 0;
		matrix[Matrix4d.M30] = 0;
		matrix[Matrix4d.M31] = 0;
		matrix[Matrix4d.M32] = 0;
		matrix[Matrix4d.M33] = 1;
	}

	/** Sets the quaternion to an identity Quaternion
	 * @return this quaternion for chaining */
	public Quat4d idt () {
		return this.set(0, 0, 0, 1);
	}

	/** @return If this quaternion is an identity Quaternion */
	public boolean isIdentity () {
		return Math.abs(x) < 1e-5 && Math.abs(y) < 1e-5 && Math.abs(z) < 1e-5 && Math.abs(w - 1) < 1e-5;
	}

	/** @return If this quaternion is an identity Quaternion */
	public boolean isIdentity (final double tolerance) {
		return Math.abs(x) < tolerance && Math.abs(y) < tolerance && Math.abs(z) < tolerance
			&& Math.abs(w - 1) < tolerance;
	}

	// todo : the setFromAxis(v3,double) method should replace the set(v3,double) method
	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param degrees The angle in degrees
	 * @return This quaternion for chaining. */
	public Quat4d setFromAxis (final Vector3d axis, final double degrees) {
		return setFromAxis(axis.x, axis.y, axis.z, degrees);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param radians The angle in radians
	 * @return This quaternion for chaining. */
	public Quat4d setFromAxisRad (final Vector3d axis, final double radians) {
		return setFromAxisRad(axis.x, axis.y, axis.z, radians);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param degrees The angle in degrees
	 * @return This quaternion for chaining. */
	public Quat4d setFromAxis (final double x, final double y, final double z, final double degrees) {
		return setFromAxisRad(x, y, z, degrees * degreesToRadians);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param radians The angle in radians
	 * @return This quaternion for chaining. */
	public Quat4d setFromAxisRad (final double x, final double y, final double z, final double radians) {
		double d = Vector3d.len(x, y, z);
		if (d == 0) return idt();
		d = 1 / d;
		double l_ang = radians < 0 ? PI2 - (-radians % PI2) : radians % PI2;
		double l_sin = Math.sin(l_ang / 2);
		double l_cos = Math.cos(l_ang / 2);
		return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).nor();
	}

	/** Sets the Quaternion from the given matrix, optionally removing any scaling. */
	public Quat4d setFromMatrix (boolean normalizeAxes, Matrix4d matrix) {
		return setFromAxes(normalizeAxes, matrix.val[Matrix4d.M00], matrix.val[Matrix4d.M01], matrix.val[Matrix4d.M02],
			matrix.val[Matrix4d.M10], matrix.val[Matrix4d.M11], matrix.val[Matrix4d.M12], matrix.val[Matrix4d.M20],
			matrix.val[Matrix4d.M21], matrix.val[Matrix4d.M22]);
	}

	/** Sets the Quaternion from the given rotation matrix, which must not contain scaling. */
	public Quat4d setFromMatrix (Matrix4d matrix) {
		return setFromMatrix(false, matrix);
	}

	/** Sets the Quaternion from the given matrix, optionally removing any scaling. */
	public Quat4d setFromMatrix (boolean normalizeAxes, Matrix3d matrix) {
		return setFromAxes(normalizeAxes, matrix.val[Matrix3d.M00], matrix.val[Matrix3d.M01], matrix.val[Matrix3d.M02],
			matrix.val[Matrix3d.M10], matrix.val[Matrix3d.M11], matrix.val[Matrix3d.M12], matrix.val[Matrix3d.M20],
			matrix.val[Matrix3d.M21], matrix.val[Matrix3d.M22]);
	}

	/** Sets the Quaternion from the given rotation matrix, which must not contain scaling. */
	public Quat4d setFromMatrix (Matrix3d matrix) {
		return setFromMatrix(false, matrix);
	}

	/** <p>
	 * Sets the Quaternion from the given x-, y- and z-axis which have to be orthonormal.
	 * </p>
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
	 * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
	 * </p>
	 * 
	 * @param xx x-axis x-coordinate
	 * @param xy x-axis y-coordinate
	 * @param xz x-axis z-coordinate
	 * @param yx y-axis x-coordinate
	 * @param yy y-axis y-coordinate
	 * @param yz y-axis z-coordinate
	 * @param zx z-axis x-coordinate
	 * @param zy z-axis y-coordinate
	 * @param zz z-axis z-coordinate */
	public Quat4d setFromAxes (double xx, double xy, double xz, double yx, double yy, double yz, double zx, double zy, double zz) {
		return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
	}

	/** <p>
	 * Sets the Quaternion from the given x-, y- and z-axis.
	 * </p>
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
	 * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
	 * </p>
	 * 
	 * @param normalizeAxes whether to normalize the axes (necessary when they contain scaling)
	 * @param xx x-axis x-coordinate
	 * @param xy x-axis y-coordinate
	 * @param xz x-axis z-coordinate
	 * @param yx y-axis x-coordinate
	 * @param yy y-axis y-coordinate
	 * @param yz y-axis z-coordinate
	 * @param zx z-axis x-coordinate
	 * @param zy z-axis y-coordinate
	 * @param zz z-axis z-coordinate */
	public Quat4d setFromAxes (boolean normalizeAxes, double xx, double xy, double xz, double yx, double yy, double yz, double zx,
							   double zy, double zz) {
		if (normalizeAxes) {
			final double lx = 1 / Vector3d.len(xx, xy, xz);
			final double ly = 1 / Vector3d.len(yx, yy, yz);
			final double lz = 1 / Vector3d.len(zx, zy, zz);
			xx *= lx;
			xy *= lx;
			xz *= lx;
			yx *= ly;
			yy *= ly;
			yz *= ly;
			zx *= lz;
			zy *= lz;
			zz *= lz;
		}
		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		final double t = xx + yy + zz;

		// we protect the division by s by ensuring that s>=1
		if (t >= 0) { // |w| >= .5
			double s = Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5 * s;
			s = 0.5 / s; // so this division isn't bad
			x = (zy - yz) * s;
			y = (xz - zx) * s;
			z = (yx - xy) * s;
		} else if ((xx > yy) && (xx > zz)) {
			double s = Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
			x = s * 0.5; // |x| >= .5
			s = 0.5 / s;
			y = (yx + xy) * s;
			z = (xz + zx) * s;
			w = (zy - yz) * s;
		} else if (yy > zz) {
			double s = Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
			y = s * 0.5; // |y| >= .5
			s = 0.5 / s;
			x = (yx + xy) * s;
			z = (zy + yz) * s;
			w = (xz - zx) * s;
		} else {
			double s = Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
			z = s * 0.5; // |z| >= .5
			s = 0.5 / s;
			x = (xz + zx) * s;
			y = (zy + yz) * s;
			w = (yx - xy) * s;
		}

		return this;
	}

	/** Set this quaternion to the rotation between two vectors.
	 * @param v1 The base vector, which should be normalized.
	 * @param v2 The target vector, which should be normalized.
	 * @return This quaternion for chaining */
	public Quat4d setFromCross (final Vector3d v1, final Vector3d v2) {
		final double dot = MathHelper.clamp(v1.dot(v2), -1, 1);
		final double angle = Math.acos(dot);
		return setFromAxisRad(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x, angle);
	}

	/** Set this quaternion to the rotation between two vectors.
	 * @param x1 The base vectors x value, which should be normalized.
	 * @param y1 The base vectors y value, which should be normalized.
	 * @param z1 The base vectors z value, which should be normalized.
	 * @param x2 The target vector x value, which should be normalized.
	 * @param y2 The target vector y value, which should be normalized.
	 * @param z2 The target vector z value, which should be normalized.
	 * @return This quaternion for chaining */
	public Quat4d setFromCross (final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		final double dot = MathHelper.clamp(Vector3d.dot(x1, y1, z1, x2, y2, z2), -1, 1);
		final double angle = Math.acos(dot);
		return setFromAxisRad(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
	}

	/** Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
	 * [0,1]. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
	 * @param end the end quaternion
	 * @param alpha alpha in the range [0,1]
	 * @return this quaternion for chaining */
	public Quat4d slerp (Quat4d end, double alpha) {
		final double d = this.x * end.x + this.y * end.y + this.z * end.z + this.w * end.w;
		double absDot = d < 0. ? -d : d;

		// Set the first and second scale for the interpolation
		double scale0 = 1 - alpha;
		double scale1 = alpha;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			final double angle = Math.acos(absDot);
			final double invSinTheta = 1 / Math.sin(angle);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = (Math.sin((1 - alpha) * angle) * invSinTheta);
			scale1 = (Math.sin((alpha * angle)) * invSinTheta);
		}

		if (d < 0.) scale1 = -scale1;

		// Calculate the x, y, z and w values for the quaternion by using a
		// special form of linear interpolation for quaternions.
		x = (scale0 * x) + (scale1 * end.x);
		y = (scale0 * y) + (scale1 * end.y);
		z = (scale0 * z) + (scale1 * end.z);
		w = (scale0 * w) + (scale1 * end.w);

		// Return the interpolated quaternion
		return this;
	}

	/** Spherical linearly interpolates multiple quaternions and stores the result in this Quaternion. Will not destroy the data
	 * previously inside the elements of q. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where w_i=1/n.
	 * @param q List of quaternions
	 * @return This quaternion for chaining */
	public Quat4d slerp (Quat4d[] q) {

		// Calculate exponents and multiply everything from left to right
		final double w = 1.0 / q.length;
		set(q[0]).exp(w);
		for (int i = 1; i < q.length; i++)
			mul(tmp1.set(q[i]).exp(w));
		nor();
		return this;
	}

	/** Spherical linearly interpolates multiple quaternions by the given weights and stores the result in this Quaternion. Will not
	 * destroy the data previously inside the elements of q or w. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where the sum of w_i
	 * is 1. Lists must be equal in length.
	 * @param q List of quaternions
	 * @param w List of weights
	 * @return This quaternion for chaining */
	public Quat4d slerp (Quat4d[] q, double[] w) {

		// Calculate exponents and multiply everything from left to right
		set(q[0]).exp(w[0]);
		for (int i = 1; i < q.length; i++)
			mul(tmp1.set(q[i]).exp(w[i]));
		nor();
		return this;
	}

	/** Calculates (this quaternion)^alpha where alpha is a real number and stores the result in this quaternion. See
	 * http://en.wikipedia.org/wiki/Quaternion#Exponential.2C_logarithm.2C_and_power
	 * @param alpha Exponent
	 * @return This quaternion for chaining */
	public Quat4d exp (double alpha) {

		// Calculate |q|^alpha
		double norm = len();
		double normExp = Math.pow(norm, alpha);

		// Calculate theta
		double theta = Math.acos(w / norm);

		// Calculate coefficient of basis elements
		double coeff = 0;
		if (Math.abs(theta) < 0.001) // If theta is small enough, use the limit of sin(alpha*theta) / sin(theta) instead of actual
// value
			coeff = normExp * alpha / norm;
		else
			coeff = (normExp * Math.sin(alpha * theta) / (norm * Math.sin(theta)));

		// Write results
		w = (normExp * Math.cos(alpha * theta));
		x *= coeff;
		y *= coeff;
		z *= coeff;

		// Fix any possible discrepancies
		nor();

		return this;
	}

	/** Get the dot product between the two quaternions (commutative).
	 * @param x1 the x component of the first quaternion
	 * @param y1 the y component of the first quaternion
	 * @param z1 the z component of the first quaternion
	 * @param w1 the w component of the first quaternion
	 * @param x2 the x component of the second quaternion
	 * @param y2 the y component of the second quaternion
	 * @param z2 the z component of the second quaternion
	 * @param w2 the w component of the second quaternion
	 * @return the dot product between the first and second quaternion. */
	public final static double dot (final double x1, final double y1, final double z1, final double w1, final double x2, final double y2,
		final double z2, final double w2) {
		return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
	}

	/** Get the dot product between this and the other quaternion (commutative).
	 * @param other the other quaternion.
	 * @return the dot product of this and the other quaternion. */
	public double dot (final Quat4d other) {
		return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
	}

	/** Get the dot product between this and the other quaternion (commutative).
	 * @param x the x component of the other quaternion
	 * @param y the y component of the other quaternion
	 * @param z the z component of the other quaternion
	 * @param w the w component of the other quaternion
	 * @return the dot product of this and the other quaternion. */
	public double dot (final double x, final double y, final double z, final double w) {
		return this.x * x + this.y * y + this.z * z + this.w * w;
	}

	/** Multiplies the components of this quaternion with the given scalar.
	 * @param scalar the scalar.
	 * @return this quaternion for chaining. */
	public Quat4d mul (double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		this.w *= scalar;
		return this;
	}

	/** Get the axis angle representation of the rotation in degrees. The supplied vector will receive the axis (x, y and z values)
	 * of the rotation and the value returned is the angle in degrees around that axis. Note that this method will alter the
	 * supplied vector, the existing value of the vector is ignored. </p> This will normalize this quaternion if needed. The
	 * received axis is a unit vector. However, if this is an identity quaternion (no rotation), then the length of the axis may be
	 * zero.
	 * 
	 * @param axis vector which will receive the axis
	 * @return the angle in degrees
	 * @see <a href="http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">wikipedia</a>
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle">calculation</a> */
	public double getAxisAngle (Vector3d axis) {
		return getAxisAngleRad(axis) * radiansToDegrees;
	}

	/** Get the axis-angle representation of the rotation in radians. The supplied vector will receive the axis (x, y and z values)
	 * of the rotation and the value returned is the angle in radians around that axis. Note that this method will alter the
	 * supplied vector, the existing value of the vector is ignored. </p> This will normalize this quaternion if needed. The
	 * received axis is a unit vector. However, if this is an identity quaternion (no rotation), then the length of the axis may be
	 * zero.
	 * 
	 * @param axis vector which will receive the axis
	 * @return the angle in radians
	 * @see <a href="http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">wikipedia</a>
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle">calculation</a> */
	public double getAxisAngleRad (Vector3d axis) {
		if (this.w > 1) this.nor(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
		double angle = (2.0 * Math.acos(this.w));
		double s = Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less than 1, so term always positive.
		if (s < 1e-5) { // test to avoid divide by zero, s is always positive due to sqrt
			// if s close to zero then direction of axis not important
			axis.x = this.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
			axis.y = this.y;
			axis.z = this.z;
		} else {
			axis.x = (this.x / s); // normalise axis
			axis.y = (this.y / s);
			axis.z = (this.z / s);
		}

		return angle;
	}

	/** Get the angle in radians of the rotation this quaternion represents. Does not normalize the quaternion. Use
	 * {@link #getAxisAngleRad(Vector3d)} to get both the axis and the angle of this rotation. Use
	 * {@link #getAngleAroundRad(Vector3d)} to get the angle around a specific axis.
	 * @return the angle in radians of the rotation */
	public double getAngleRad () {
		return (2.0 * Math.acos((this.w > 1) ? (this.w / len()) : this.w));
	}

	/** Get the angle in degrees of the rotation this quaternion represents. Use {@link #getAxisAngle(Vector3d)} to get both the axis
	 * and the angle of this rotation. Use {@link #getAngleAround(Vector3d)} to get the angle around a specific axis.
	 * @return the angle in degrees of the rotation */
	public double getAngle () {
		return getAngleRad() * radiansToDegrees;
	}

	/** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
	 * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
	 * axis perpendicular to the specified axis. </p> The swing and twist rotation can be used to reconstruct the original
	 * quaternion: this = swing * twist
	 * 
	 * @param axisX the X component of the normalized axis for which to get the swing and twist rotation
	 * @param axisY the Y component of the normalized axis for which to get the swing and twist rotation
	 * @param axisZ the Z component of the normalized axis for which to get the swing and twist rotation
	 * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
	 * @param twist will receive the twist rotation: the rotation around the specified axis
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
	public void getSwingTwist (final double axisX, final double axisY, final double axisZ, final Quat4d swing,
		final Quat4d twist) {
		final double d = Vector3d.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
		twist.set(axisX * d, axisY * d, axisZ * d, this.w).nor();
		if (d < 0) twist.mul(-1);
		swing.set(twist).conjugate().mulLeft(this);
	}

	/** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
	 * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
	 * axis perpendicular to the specified axis. </p> The swing and twist rotation can be used to reconstruct the original
	 * quaternion: this = swing * twist
	 * 
	 * @param axis the normalized axis for which to get the swing and twist rotation
	 * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
	 * @param twist will receive the twist rotation: the rotation around the specified axis
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition">calculation</a> */
	public void getSwingTwist (final Vector3d axis, final Quat4d swing, final Quat4d twist) {
		getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
	}

	/** Get the angle in radians of the rotation around the specified axis. The axis must be normalized.
	 * @param axisX the x component of the normalized axis for which to get the angle
	 * @param axisY the y component of the normalized axis for which to get the angle
	 * @param axisZ the z component of the normalized axis for which to get the angle
	 * @return the angle in radians of the rotation around the specified axis */
	public double getAngleAroundRad (final double axisX, final double axisY, final double axisZ) {
		final double d = Vector3d.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
		final double l2 = Quat4d.len2(axisX * d, axisY * d, axisZ * d, this.w);
		return Math.abs(l2) < 1e-5 ? 0 : (2.0 * Math.acos(MathHelper.clamp(
			((d < 0 ? -this.w : this.w) / Math.sqrt(l2)), -1, 1)));
	}

	/** Get the angle in radians of the rotation around the specified axis. The axis must be normalized.
	 * @param axis the normalized axis for which to get the angle
	 * @return the angle in radians of the rotation around the specified axis */
	public double getAngleAroundRad (final Vector3d axis) {
		return getAngleAroundRad(axis.x, axis.y, axis.z);
	}

	/** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized.
	 * @param axisX the x component of the normalized axis for which to get the angle
	 * @param axisY the y component of the normalized axis for which to get the angle
	 * @param axisZ the z component of the normalized axis for which to get the angle
	 * @return the angle in degrees of the rotation around the specified axis */
	public double getAngleAround (final double axisX, final double axisY, final double axisZ) {
		return getAngleAroundRad(axisX, axisY, axisZ) * radiansToDegrees;
	}

	/** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized.
	 * @param axis the normalized axis for which to get the angle
	 * @return the angle in degrees of the rotation around the specified axis */
	public double getAngleAround (final Vector3d axis) {
		return getAngleAround(axis.x, axis.y, axis.z);
	}
}
