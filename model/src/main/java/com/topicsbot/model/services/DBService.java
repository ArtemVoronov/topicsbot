package com.topicsbot.model.services;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

public interface DBService {
  void destroy();

  <T> T tx(Tx<T> action);

  void vtx(VoidTx action);

  <T> T stateless(Stateless<T> action);

  void vstateless(VoidStateless action);

  interface Tx<T> {
    T run(Session s);
  }

  interface VoidTx {
    void run(Session s);
  }

  interface Stateless<T> {
    T run(StatelessSession s);
  }

  interface VoidStateless {
    void run(StatelessSession s);
  }
}
