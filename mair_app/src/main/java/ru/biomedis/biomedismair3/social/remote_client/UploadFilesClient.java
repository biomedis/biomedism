package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;
import java.util.Date;
import java.util.List;
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData;
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData;
import ru.biomedis.biomedismair3.social.remote_client.dto.FileType;

public interface UploadFilesClient {

  @RequestLine("POST /uploadFile/to_directory/{directory}")
  @Headers("Content-Type: multipart/form-data")
  FileData uploadFile(@Param("file") FormData file, @Param("directory") long directory);

  @Headers("Content-Type: multipart/form-data")
  @RequestLine("POST /backup")
  Date doBackup(@Param("file") FormData file);

}
