package com.example.fibonacci;
 
import com.fasterxml.jackson.annotation.JsonCreator;

public class Number {

  public final long value;

  @JsonCreator
  public Number(long value) {
    this.value = value;
  }
}
