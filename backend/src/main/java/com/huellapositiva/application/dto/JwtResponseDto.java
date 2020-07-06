package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
public class JwtResponseDto {
    @Schema(
            description = "Access token contains login credentials (such as the user ID and his roles) in order to verify that the user can access to the given endpoint",
            example = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..qPxdcrmy04En8lrP.JtPX2HK7gApQHR5R_8DjoHZq703Mpl2eiBl4GN-SrEb72lPBeb_CRULKxGAMveQ5WHaHHZJ9TC6-GA37v7bHLPSQrrMZonZCCUhYNl2afPpzYkHwJOKeTRLl3Kx339VJLOhCgtyhxP5Ca_oWW0Um4ke6XYo6pK1uNPncwXmivdvOmQzGEMHslNehJpcdxUkwn7Qw7TU1tUEfDqBUp5c8jOtSaPF6Nui12aKlHrFKn_dKUsDIdhTkBIROipec9wriyF_fMW3pQ34TiYz48aubvmqPAkVWrOLB0BfDapG0LsRsvoAWyr5e9HHa48SYvnb-mKXmhOS5-K8LlOoOLMb6AJuCgQ.IUVXVJP1CcDzwr4mnE_Psw"
    )
    String accessToken;

    @Schema(
            description = "Refresh token contains the user ID and its function is to create new Access and Refresh tokens. This action is mandatory when the access token has expired or revoked ",
            example = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..qoPv5OowzuLJEiW8.HZKlt617rEYownhiN8BRsRz6LCXFLA_5aeLVJ4IfDNSgZH97Z6HQvTtPAozoeQ1Q26M-a4m-oQVx-z-I0M-rfpYaYTql6T9ynR9ZMMJytMUZhU7wVnZ7yiuqnAbanoyusw67pVsF_DkTmSmiKSu-98iBJkLvdXfqyuBfwcqfaxjjPn8QInukJITlkRNPYcJpBmQJkHPF49QhC3hlyUhxY8wxC87xr3Ih3O4SgYoTqUAi55XnCENVio1xfrWpAjpO5xcmLf1VOomEhB74gwx8RtjcHLkWjk0Ka2xhtlyd1TrU-Q.TP4B84yMQxb6214VzmBFsA"

    )
    String refreshToken;
}
