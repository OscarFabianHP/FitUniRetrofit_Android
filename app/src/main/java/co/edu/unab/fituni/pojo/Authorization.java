package co.edu.unab.fituni.pojo;

import com.google.gson.annotations.SerializedName;

public class Authorization {
    @SerializedName("access") //se mapea nombre segun se llame en el backend de springboot
    private String token;

    public String getToken() {
        return token;
    }
}
