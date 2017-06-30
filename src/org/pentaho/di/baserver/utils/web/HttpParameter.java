package org.pentaho.di.baserver.utils.web;

/**
 * Created by Comp on 02.07.2017.
 */
public class HttpParameter {

  public enum ParamType{
      QUERY, BODY, NONE
  }

    public HttpParameter(String name, String value, ParamType paramType) {
        this.name = name;
        this.value = value;
        this.paramType = paramType;
    }

  private String name;
  private String value;
  private ParamType paramType;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }
}
