/*
 * Copyright 1998-2014 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.meteoinfo.ndarray;

/**
 * Specialization of Index for rank 2 arrays.
 *
 * @see Index
 * @author caron
 */
public class Index2D extends Index {

  /** current element's indices */
  private int curr0, curr1;
  /** array strides */
  private int stride0, stride1;
  /** array shapes */
  private int shape0, shape1;

  Index2D() { super(2); }
  public Index2D( int[] shape) {
    super(shape);
    precalc();
  }

  protected void precalc() {
    shape0 = shape[0];
    shape1 = shape[1];

    stride0 = stride[0];
    stride1 = stride[1];

    curr0 = current[0];
    curr1 = current[1];
  }

  public int [] getCurrentCounter() {
    current[0] = curr0;
    current[1] = curr1;
    return current.clone();
  }

   public String toString() {
     return curr0+","+curr1;
   }

  public int currentElement() {
    return offset + curr0*stride0 + curr1*stride1;
  }

  public int incr() {
      if (++curr1 >= shape1) {
        curr1 = 0;
        if (++curr0 >= shape0) {
          curr0 = 0;    // rollover !
        }
      }
    return offset + curr0*stride0 + curr1*stride1;
  }


  public void setDim(int dim, int value) {
    if (value < 0 || value >= shape[dim])  // check index here
      throw new ArrayIndexOutOfBoundsException();
    if (dim == 1)
      curr1 = value;
    else
      curr0 = value;
  }

  public Index set(int[] index){
    if (index.length != rank)
      throw new ArrayIndexOutOfBoundsException();
    set0(index[0]);
    set1(index[1]);
    return this;
  }

  public Index set0(int v) {
    if (v < 0 || v >= shape0)  // check index here
      throw new ArrayIndexOutOfBoundsException();
    curr0 = v;
    return this;
  }

  public Index set1(int v) {
    if (v < 0 || v >= shape1)  // check index here
      throw new ArrayIndexOutOfBoundsException("index="+v+" shape="+shape1);
    curr1 = v;
    return this;
  }

  public Index set(int v0, int v1) {
    set0(v0);
    set1(v1);
    return this;
  }

  public Object clone() {
    return super.clone();
  }

  int setDirect(int v0, int v1) {
    if (v0 < 0 || v0 >= shape0)
      throw new ArrayIndexOutOfBoundsException();
    if (v1 < 0 || v1 >= shape1)
      throw new ArrayIndexOutOfBoundsException();
    return offset + v0*stride0 + v1*stride1;
  }

}
