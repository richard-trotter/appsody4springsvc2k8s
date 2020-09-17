package demo.inventory.api.model;

public class ApiError {

  public int statusCode;
  public String errorMessage;
  public String errorDetailMessage;
  
  public ApiError() {
    super();
  }
  
  public ApiError(int statusCode, String errorMessage, String errorDetailMessage) {

    super();
    
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
    this.errorDetailMessage = errorDetailMessage;
  }

  @Override
  public String toString() {
    return "ApiError [statusCode=" + statusCode 
        + ", errorMessage=" + errorMessage 
        + ", errorDetailMessage=" + errorDetailMessage + "]";
  }
  
}
