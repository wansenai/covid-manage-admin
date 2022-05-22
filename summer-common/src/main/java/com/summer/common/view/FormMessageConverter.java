package com.summer.common.view;

import com.google.common.collect.Lists;
import com.summer.common.helper.BytesHelper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;

import java.util.List;

public final class FormMessageConverter extends FormHttpMessageConverter {

    public static final FormMessageConverter INSTANCE = new FormMessageConverter();

    private FormMessageConverter() {
        setCharset(BytesHelper.UTF8);
        List<HttpMessageConverter<?>> converters = Lists.newArrayList();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(StringMessageConverter.INSTANCE);
        converters.add(new SourceHttpMessageConverter());
        super.setPartConverters(converters);
        super.setSupportedMediaTypes(Lists.newArrayList(MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA));
    }
}
