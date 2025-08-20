package com.devmate.userservice.utils;

import java.util.BitSet;

public class GeoHash {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int[] BITS = {16, 8, 4, 2, 1};

    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        boolean isEven = true;
        BitSet buffer = new BitSet(precision * 5);
        int bit = 0;

        while (bit < precision * 5) {
            if (isEven) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude > mid) {
                    buffer.set(bit);
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude > mid) {
                    buffer.set(bit);
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }
            isEven = !isEven;
            bit++;
        }

        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < precision; i++) {
            int idx = 0;
            for (int j = 0; j < 5; j++) {
                if (buffer.get(i * 5 + j)) {
                    idx += BITS[j];
                }
            }
            hash.append(BASE32.charAt(idx));
        }
        return hash.toString();
    }

    public static String[] getAdjacentHashes(String geohash) {
        String[] adjacent = new String[8];
        adjacent[0] = adjacent(geohash, Direction.TOP);
        adjacent[1] = adjacent(geohash, Direction.BOTTOM);
        adjacent[2] = adjacent(geohash, Direction.LEFT);
        adjacent[3] = adjacent(geohash, Direction.RIGHT);
        adjacent[4] = adjacent(adjacent[0], Direction.LEFT);
        adjacent[5] = adjacent(adjacent[0], Direction.RIGHT);
        adjacent[6] = adjacent(adjacent[1], Direction.LEFT);
        adjacent[7] = adjacent(adjacent[1], Direction.RIGHT);
        return adjacent;
    }

    private enum Direction { TOP, BOTTOM, LEFT, RIGHT }

    private static String adjacent(String hash, Direction direction) {
        // Implementation of adjacent geohash calculation
        // (Available in complete geohash libraries)
    }
}