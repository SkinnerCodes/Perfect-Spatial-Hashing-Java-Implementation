package Thomas;

public class Vec3I {

    public int x, y, z;

    public Vec3I() {}

    public Vec3I(Vec3I vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    public Vec3I(int amplitutde) {
        this.x = amplitutde;
        this.y = amplitutde;
        this.z = amplitutde;
    }

    public Vec3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void  mod(Vec3I rhs) {
        x = x % rhs.x;
        y = y % rhs.y;
        z = z % rhs.z;
        this.add(rhs);
        x = x % rhs.x;
        y = y % rhs.y;
        z = z % rhs.z;

    }
    public Vec3I add(Vec3I rhs) {
        x += rhs.x;
        y += rhs.y;
        z += rhs.z;
        return this;
    }
    public void add(int rhs) {
        x += rhs;
        y += rhs;
        z += rhs;
    }
    public Vec3I mul(int rhs) {
        x *= rhs;
        y *= rhs;
        z *= rhs;
        return this;

    }
    public void div(int rhs) {
        x /= rhs; y /= rhs; z /= rhs;
    }
    //-------------------------------------------------------------------------------
    //  Checks if this vector is within the bounding box represented by the inclusive
    //  minimum bound vector and the exclusive maximum bound vector.
    //-------------------------------------------------------------------------------
    public boolean isInBounds(Vec3I minBound, Vec3I maxBound) {
        if (this.x < minBound.x)
            return false;
        if (this.y < minBound.y)
            return false;
        if (this.z < minBound.z)
            return false;
        if (this.x >= maxBound.x)
            return false;
        if (this.y >= maxBound.y)
            return false;
        if (this.z >= maxBound.z)
            return false;
        return true;
    }

    public static Vec3I add(Vec3I lhs, int rhs) {
        return new Vec3I(lhs.x + rhs, lhs.y + rhs, lhs.z + rhs);
    }

    public static Vec3I add(Vec3I lhs, Vec3I rhs){
        return new Vec3I(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z);
    }

    public static Vec3I subtract(Vec3I lhs, Vec3I rhs) {
        return new Vec3I(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z);
    }
    public static Vec3I subtract(Vec3I lhs, int rhs) {
        return new Vec3I(lhs.x - rhs, lhs.y - rhs, lhs.z - rhs);
    }
    public static Vec3I mul(Vec3I lhs, Vec3I rhs) {
        return new Vec3I(lhs.x * rhs.x, lhs.y * rhs.y, lhs.z * rhs.z);
    }
    public static Vec3I mul(Vec3I lhs, int rhs) {
        return new Vec3I(lhs.x * rhs, lhs.y * rhs, lhs.z * rhs);
    }
    public static Vec3I div(Vec3I lhs, int rhs) {
        return new Vec3I(lhs.x / rhs, lhs.y / rhs, lhs.z / rhs);
    }
//    public static Vec3I div(Vec3 lhs, Vec3I rhs) {
//        return new Vec3I((int)(lhs.x / rhs.x), (int)(lhs.y / rhs.y), (int)(lhs.z / rhs.z));
//    }
//    public static Vec3I div(Vec3 lhs, int rhs) {
//        return new Vec3I((int)(lhs.x / rhs), (int)(lhs.y / rhs), (int)(lhs.z / rhs));
//    }

    public static Vec3I mod(Vec3I lhs, Vec3I rhs) {
        Vec3I answer = new Vec3I((lhs.x % rhs.x), (lhs.y % rhs.y) ,(lhs.z % rhs.z));
        answer.add(rhs);
        answer.x %= rhs.x;
        answer.y %= rhs.y;
        answer.z %= rhs.z;
        return answer;
    }

    public static Vec3I mod(Vec3I lhs, int rhs) {
        Vec3I answer = new Vec3I((lhs.x % rhs), (lhs.y % rhs) ,(lhs.z % rhs));
        answer.add(rhs);
        answer.x %= rhs;
        answer.y %= rhs;
        answer.z %= rhs;
        return answer;
    }


    @Override
    public String toString() {
        return String.format("{X:%s, Y:%s, Z:%s}", String.valueOf(x), String.valueOf(y), String.valueOf(z));
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + x;
        result = result * 31 + y;
        result = result * 31 + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Vec3I))
            return false;
        Vec3I v3 = (Vec3I) obj;
        if (this.x != v3.x)
            return false;
        if (this.y != v3.y)
            return false;
        if (this.z != v3.z)
            return false;
        return true;

    }


}
