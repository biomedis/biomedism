package ru.biomedis.biomedismair3.social.remote_client.dto.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(include = As.PROPERTY, use = JsonTypeInfo.Id.CUSTOM, property = "@suberror_type", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
@JsonIgnoreProperties(value = {"@suberror_type"})
public class ApiSubError {

}
