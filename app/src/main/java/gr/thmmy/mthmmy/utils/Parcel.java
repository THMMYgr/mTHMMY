package gr.thmmy.mthmmy.utils;

public class Parcel<T> {
    private int resultCode;
    private T data;

    public Parcel(int resultCode, T data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public T getData() {
        return data;
    }
}
