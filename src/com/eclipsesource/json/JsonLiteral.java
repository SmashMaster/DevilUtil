/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.eclipsesource.json;

import java.io.IOException;


@SuppressWarnings("serial") // use default serial UID
final class JsonLiteral extends JsonValue {
  
  private enum Type {
    NULL("null"),
    TRUE("true"),
    FALSE("false");
    
    private final String value;
    
    private Type(String value) {
      this.value = value;
    }
  }
  
  static JsonLiteral makeNull() {
    return new JsonLiteral(Type.NULL);
  }
  
  static JsonLiteral makeTrue() {
    return new JsonLiteral(Type.TRUE);
  }
  
  static JsonLiteral makeFalse() {
    return new JsonLiteral(Type.FALSE);
  }
  
  static JsonLiteral makeBoolean(boolean value) {
    return value ? makeTrue() : makeFalse();
  }
  
  private final Type type;

  private JsonLiteral(Type type) {
    this.type = type;
  }
  
  @Override
  void write(JsonWriter writer) throws IOException {
    writer.writeLiteral(type.value);
  }

  @Override
  public String toString() {
    return type.value;
  }

  @Override
  public int hashCode() {
    return type.value.hashCode();
  }

  @Override
  public boolean isNull() {
    return type == Type.NULL;
  }

  @Override
  public boolean isTrue() {
    return type == Type.TRUE;
  }

  @Override
  public boolean isFalse() {
    return type == Type.FALSE;
  }

  @Override
  public boolean isBoolean() {
    return isTrue() || isFalse();
  }

  @Override
  public boolean asBoolean() {
    return isNull() ? super.asBoolean() : isTrue();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    JsonLiteral other = (JsonLiteral)object;
    return type.equals(other.type);
  }

}
