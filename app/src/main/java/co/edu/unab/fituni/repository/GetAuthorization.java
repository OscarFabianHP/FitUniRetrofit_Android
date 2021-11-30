package co.edu.unab.fituni.repository;

import co.edu.unab.fituni.modelo.Usuario;
import co.edu.unab.fituni.pojo.Authorization;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GetAuthorization {

    @Headers("Cache-Control: no-cache")
    @POST("auth")
    Call<Authorization> getAuthorization(@Body Usuario usuario);
}
