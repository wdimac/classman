package com.appdynamics.aws;

import java.util.ArrayList;

public class QuickList<E> extends ArrayList<E> {
  private static final long serialVersionUID = 1L;

  @SafeVarargs
  public QuickList(E... elements) {
    super(elements.length);

    for (E element: elements){
      this.add(element);
    }
  }
}
