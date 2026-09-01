package com.ecommerceproject.orderservice.services;

import io.jsonwebtoken.Claims;

public interface ITokenService {

    boolean validateToken(String token);

    Claims getPayload(String token);
}
