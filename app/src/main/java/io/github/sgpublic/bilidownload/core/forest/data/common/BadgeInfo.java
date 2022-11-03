package io.github.sgpublic.bilidownload.core.forest.data.common;

import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/21 14:32
 */
@Data
public class BadgeInfo {
    private String bgColor;
    private String bgColorNight;
    /** API 控制的图片 */
    private String img;
    private String text;
}