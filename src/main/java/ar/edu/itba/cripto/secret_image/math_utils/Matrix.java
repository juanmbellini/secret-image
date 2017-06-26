package ar.edu.itba.cripto.secret_image.math_utils;

/*
 * Gauss-Jordan elimination over any field (Java)
 *
 * Copyright (c) 2017 Project Nayuki
 * All rights reserved. Contact Nayuki for licensing.
 * https://www.nayuki.io/page/gauss-jordan-elimination-over-any-field
 */


import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a mutable matrix of field elements, supporting linear algebra operations.
 * Note that the dimensions of a matrix cannot be changed after construction
 *
 * @implNote This is a not thread-safe class.
 */
public final class Matrix<E> {


    // ====================================================
    // Fields
    // ====================================================

    /**
     * The values of the matrix, stored in row-major order.
     */
    private final E[][] values;
    /**
     * The field used to operate on the values in the matrix.
     */
    private final Field<E> field;


    // ====================================================
    // Constructors
    // ====================================================

    /**
     * Constructs a blank matrix with the specified number of rows and columns,
     * with operations from the specified field. All the elements are initially {@code null}.
     *
     * @param rows  the number of rows in this matrix
     * @param cols  the number of columns in this matrix
     * @param field the field used to operate on the values in this matrix
     * @throws IllegalArgumentException if {@code field} is {@code null}.
     */
    public Matrix(int rows, int cols, Field<E> field) {
        //noinspection unchecked
        this((E[][]) new Object[rows][cols], field);
    }

    /**
     * Constructs a matrix using the specified data.
     *
     * @param data  The objects to be stored in this matrix.
     * @param field The field used to operate on the values in this matrix.
     * @throws IllegalArgumentException if any row contains a different amount of columns than the rest,
     *                                  or if {@code field} is null.
     */
    public Matrix(E[][] data, Field<E> field) {
        if (Arrays.stream(data).filter(array -> array.length != data[0].length).count() > 0) {
            throw new IllegalArgumentException("All rows must have the same amount of columns");
        }
        if (field == null) {
            throw new IllegalArgumentException();
        }
        this.field = field;
        this.values = data;
    }


    // ====================================================
    // Basic matrix methods
    // ====================================================

    /**
     * Returns the number of rows in this matrix, which is positive.
     *
     * @return the number of rows in this matrix
     */
    public int rowCount() {
        return values.length;
    }

    /**
     * Returns the number of columns in this matrix, which is positive.
     *
     * @return the number of columns in this matrix
     */
    public int columnCount() {
        return values[0].length;
    }

    /**
     * Returns the element at the specified location in this matrix. The result may be {@code null}.
     *
     * @param row the row to read from (0-based indexing)
     * @param col the column to read from (0-based indexing)
     * @return the element at the specified location in this matrix (possibly {@code null})
     * @throws IndexOutOfBoundsException if the specified row or column exceeds the bounds of the matrix
     */
    public E get(int row, int col) {
        if (row < 0 || row >= values.length || col < 0 || col >= values[row].length)
            throw new IndexOutOfBoundsException("Row or column index out of bounds");
        return values[row][col];
    }

    /**
     * Stores the specified element at the specified location in this matrix. The value to store can be {@code null}.
     *
     * @param row the row to write to (0-based indexing)
     * @param col the column to write to (0-based indexing)
     * @param val the element value to write (possibly {@code null})
     * @throws IndexOutOfBoundsException if the specified row or column exceeds the bounds of the matrix
     */
    public void set(int row, int col, E val) {
        if (row < 0 || row >= values.length || col < 0 || col >= values[0].length)
            throw new IndexOutOfBoundsException("Row or column index out of bounds");
        values[row][col] = val;
    }

    /**
     * Returns a new matrix that is equal to the transpose of this matrix. The field and elements are shallow-copied
     * because they are assumed to be immutable. Any matrix element can be {@code null} when performing this operation.
     *
     * @return a transpose of this matrix
     */
    public Matrix<E> transpose() {
        int rows = rowCount();
        int cols = columnCount();
        Matrix<E> result = new Matrix<>(cols, rows, field);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                result.values[j][i] = values[i][j];
        }
        return result;
    }

    /**
     * Return's the row in the {@code index} position.
     *
     * @param index The position of the row.
     * @return The row in the {@code index} position.
     */
    public E[] getRow(int index) {
        return Arrays.copyOf(values[index], values[index].length);
    }

    /**
     * Return's the column in the {@code index} position.
     *
     * @param index The position of the column.
     * @return The column in the {@code index} position.
     */
    public E[] getColumn(int index) {
        //noinspection unchecked
        return Arrays.stream(this.values).map(each -> each[index]).toArray(i -> (E[]) new Object[i]);
    }

    /**
     * Appends the {@code other}'s rows into {@code this} {@link Matrix}.
     *
     * @param other The other {@link Matrix}.
     * @return A new {@link Matrix} containing {@code this} matrix's rows, and the {@code other} matrix's rows appended.
     */
    public Matrix<E> appendRows(Matrix<E> other) {
        if (other == null) {
            throw new IllegalArgumentException("The other matrix must not be null");
        }
        if (this.columnCount() != other.columnCount()) {
            throw new IllegalArgumentException("Both matrices must have the same amount of columns");
        }
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("Both matrices must have the same field");
        }

        @SuppressWarnings("unchecked")
        E[][] data = Stream.concat(Arrays.stream(this.values), Arrays.stream(other.values))
                .toArray(i -> (E[][]) new Object[i][]);

        return new Matrix<>(data, this.field);
    }

    /**
     * Appends the {@code other}'s columns into {@code this} {@link Matrix}.
     *
     * @param other The other {@link Matrix}.
     * @return A new {@link Matrix} containing {@code this} matrix's columns,
     * and the {@code other} matrix's columns appended.
     */
    public Matrix<E> appendColumns(Matrix<E> other) {
        if (other == null) {
            throw new IllegalArgumentException("The other matrix must not be null");
        }
        if (this.rowCount() != other.rowCount()) {
            throw new IllegalArgumentException("Both matrices must have the same amount of rows");
        }
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("Both matrices must have the same field");
        }

        @SuppressWarnings("unchecked")
        E[][] data = IntStream.range(0, this.values.length)
                .parallel()
                .mapToObj(idx -> Stream.concat(Arrays.stream(this.values[idx]), Arrays.stream(other.values[idx])))
                .parallel()
                .map(each -> each.toArray(i -> (E[]) new Object[i]))
                .toArray(i -> (E[][]) new Object[i][]);

        return new Matrix<>(data, this.field);
    }


    /**
     * Returns a string representation of this matrix. The format is subject to change.
     *
     * @return a string representation of this matrix
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rowCount(); i++) {
            if (i > 0)
                sb.append(",\n ");
            sb.append("[");
            for (int j = 0; j < columnCount(); j++) {
                if (j > 0)
                    sb.append(", ");
                sb.append(values[i][j]);
            }
            sb.append("]");
        }
        return sb.append("]").toString();
    }

    // ====================================================
    // Simple matrix row operations
    // ====================================================

    /**
     * Swaps the two specified rows of this matrix. If the two row indices are the same, the swap is a no-op.
     * Any matrix element can be {@code null} when performing this operation.
     *
     * @param row0 one row to swap (0-based indexing)
     * @param row1 the other row to swap (0-based indexing)
     * @throws IndexOutOfBoundsException if a specified row exceeds the bounds of the matrix
     */
    public void swapRows(int row0, int row1) {
        if (row0 < 0 || row0 >= values.length || row1 < 0 || row1 >= values.length)
            throw new IndexOutOfBoundsException("Row index out of bounds");
        E[] temp = values[row0];
        values[row0] = values[row1];
        values[row1] = temp;
    }


    /**
     * Multiplies the specified row in this matrix by the specified factor. In other words, row *= factor.
     * The elements of the specified row should all be non-{@code null} when performing this operation.
     *
     * @param row    the row index to operate on (0-based indexing)
     * @param factor the factor to multiply by
     * @throws IndexOutOfBoundsException if the specified row exceeds the bounds of the matrix
     */
    public void multiplyRow(int row, E factor) {
        if (row < 0 || row >= values.length)
            throw new IndexOutOfBoundsException("Row index out of bounds");
        for (int j = 0, cols = columnCount(); j < cols; j++)
            set(row, j, field.multiply(get(row, j), factor));
    }


    /**
     * Adds the first specified row in this matrix multiplied by the specified factor to the second specified row.
     * In other words, destRow += srcRow * factor. The elements of the specified two rows
     * should all be non-{@code null} when performing this operation.
     *
     * @param srcRow  the index of the row to read and multiply (0-based indexing)
     * @param destRow the index of the row to accumulate to (0-based indexing)
     * @param factor  the factor to multiply by
     * @throws IndexOutOfBoundsException if a specified row exceeds the bounds of the matrix
     */
    public void addRows(int srcRow, int destRow, E factor) {
        if (srcRow < 0 || srcRow >= values.length || destRow < 0 || destRow >= values.length)
            throw new IndexOutOfBoundsException("Row index out of bounds");
        for (int j = 0, cols = columnCount(); j < cols; j++)
            set(destRow, j, field.add(get(destRow, j), field.multiply(get(srcRow, j), factor)));
    }


    /**
     * Returns a new matrix representing this matrix multiplied by the specified matrix. Requires the specified matrix to have
     * the same number of rows as this matrix's number of columns. Remember that matrix multiplication is not commutative.
     * All elements of both matrices should be non-{@code null} when performing this operation.
     * The time complexity of this operation is <var>O</var>(this.rows &times; this.cols &times; other.cols).
     *
     * @param other the second matrix multiplicand
     * @return the product of this matrix with the specified matrix
     * @throws NullPointerException     if the specified matrix is {@code null}
     * @throws IllegalArgumentException if the specified matrix has incompatible dimensions for multiplication
     */
    public Matrix<E> multiply(Matrix<E> other) {
        if (other == null)
            throw new NullPointerException();
        if (columnCount() != other.rowCount())
            throw new IllegalArgumentException("Incompatible matrix sizes for multiplication");

        int rows = rowCount();
        int cols = other.columnCount();
        int cells = columnCount();
        Matrix<E> result = new Matrix<>(rows, cols, field);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                E sum = field.zero();
                for (int k = 0; k < cells; k++)
                    sum = field.add(field.multiply(get(i, k), other.get(k, j)), sum);
                result.set(i, j, sum);
            }
        }
        return result;
    }


    // ====================================================
    // Advanced matrix operations
    // ====================================================

    /**
     * Converts this matrix to reduced row echelon form (RREF) using Gauss-Jordan elimination.
     * All elements of this matrix should be non-{@code null} when performing this operation.
     * Always succeeds, as long as the field follows the mathematical rules and does not throw an exception.
     * The time complexity of this operation is <var>O</var>(rows &times; cols &times; min(rows, cols)).
     *
     * @return {@code this} (for method chaining).
     */
    public Matrix<E> reducedRowEchelonForm() {
        int rows = rowCount();
        int cols = columnCount();

        // Compute row echelon form (REF)
        int numPivots = 0;
        for (int j = 0; j < cols && numPivots < rows; j++) {  // For each column
            // Find a pivot row for this column
            int pivotRow = numPivots;
            while (pivotRow < rows && field.equals(get(pivotRow, j), field.zero()))
                pivotRow++;
            if (pivotRow == rows)
                continue;  // Cannot eliminate on this column
            swapRows(numPivots, pivotRow);
            pivotRow = numPivots;
            numPivots++;

            // Simplify the pivot row
            multiplyRow(pivotRow, field.reciprocal(get(pivotRow, j)));

            // Eliminate rows below
            for (int i = pivotRow + 1; i < rows; i++)
                addRows(pivotRow, i, field.negate(get(i, j)));
        }

        // Compute reduced row echelon form (RREF)
        for (int i = numPivots - 1; i >= 0; i--) {
            // Find pivot
            int pivotCol = 0;
            while (pivotCol < cols && field.equals(get(i, pivotCol), field.zero()))
                pivotCol++;
            if (pivotCol == cols)
                continue;  // Skip this all-zero row

            // Eliminate rows above
            for (int j = i - 1; j >= 0; j--)
                addRows(i, j, field.negate(get(j, pivotCol)));
        }
        return this;
    }


    /**
     * Replaces the values of this matrix with the inverse of this matrix. Requires the matrix to be square.
     * All elements of this matrix should be non-{@code null} when performing this operation.
     * Throws an exception if the matrix is singular (not invertible). If an exception is thrown, this matrix is unchanged.
     * The time complexity of this operation is <var>O</var>(rows<sup>3</sup>).
     *
     * @throws IllegalStateException if this matrix is not square
     * @throws IllegalStateException if this matrix has no inverse
     */
    public void invert() {
        int rows = rowCount();
        int cols = columnCount();
        if (rows != cols)
            throw new IllegalStateException("Matrix dimensions are not square");

        // Build augmented matrix: [this | identity]
        Matrix<E> temp = new Matrix<>(rows, cols * 2, field);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                temp.set(i, j, get(i, j));
                temp.set(i, j + cols, i == j ? field.one() : field.zero());
            }
        }

        // Do the main calculation
        temp.reducedRowEchelonForm();

        // Check that the left half is the identity matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!field.equals(temp.get(i, j), i == j ? field.one() : field.zero()))
                    throw new IllegalStateException("Matrix is not invertible");
            }
        }

        // Extract inverse matrix from: [identity | inverse]
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                set(i, j, temp.get(i, j + cols));
        }
    }


    /**
     * Returns the determinant of this matrix, and as a side effect converts the matrix to row echelon form (REF).
     * Requires the matrix to be square. The leading coefficient of each row is not guaranteed to be one.
     * All elements of this matrix should be non-{@code null} when performing this operation.
     * Always succeeds, as long as the field follows the mathematical rules and does not throw an exception.
     * The time complexity of this operation is <var>O</var>(rows<sup>3</sup>).
     *
     * @return the determinant of this matrix
     * @throws IllegalStateException if this matrix is not square
     */
    public E determinantAndRef() {
        int rows = rowCount();
        int cols = columnCount();
        if (rows != cols)
            throw new IllegalStateException("Matrix dimensions are not square");
        E det = field.one();

        // Compute row echelon form (REF)
        int numPivots = 0;
        for (int j = 0; j < cols; j++) {  // For each column
            // Find a pivot row for this column
            int pivotRow = numPivots;
            while (pivotRow < rows && field.equals(get(pivotRow, j), field.zero()))
                pivotRow++;

            if (pivotRow < rows) {
                // This column has a nonzero pivot
                if (numPivots != pivotRow) {
                    swapRows(numPivots, pivotRow);
                    det = field.negate(det);
                }
                pivotRow = numPivots;
                numPivots++;

                // Simplify the pivot row
                E temp = get(pivotRow, j);
                multiplyRow(pivotRow, field.reciprocal(temp));
                det = field.multiply(temp, det);

                // Eliminate rows below
                for (int i = pivotRow + 1; i < rows; i++)
                    addRows(pivotRow, i, field.negate(get(i, j)));
            }

            // Update determinant
            det = field.multiply(get(j, j), det);
        }
        return det;
    }

}
