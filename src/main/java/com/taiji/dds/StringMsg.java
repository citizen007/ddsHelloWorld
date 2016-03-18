package com.taiji.dds;

// CoreDX DDL Generated code.  Do not modify - modifications may be overwritten.



public class StringMsg {
  
  // instance variables
  public String msg;
  
  // constructors
  public StringMsg() { }
  public StringMsg( String __f1 ) {
    msg = __f1;
  }
  
  public StringMsg init() { 
    msg = new String();
    return this;
  }

  public void clear() {
    msg = null;
  }
  
  public void copy( StringMsg from ) {
    this.msg = from.msg;
  }
  
}; // StringMsg
