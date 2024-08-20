package io.github.sgpublic.bilidownload.core.forest.data.common;

import lombok.Data;

@Data
public class TokenInfo {
    private String mid;
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
