package ru.biomedis.biomedismair3.social.remote_client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData;
import ru.biomedis.biomedismair3.social.remote_client.dto.FileData;
import ru.biomedis.biomedismair3.social.remote_client.dto.FileType;
import ru.biomedis.biomedismair3.social.remote_client.dto.Links;

public interface FilesClient {

  /**
   * Получит имя файла своего, защищенного или публичного. Попытка получить имя защищенного не своего файла выкенет ошибку
   * @param id
   * @return
   */
  @RequestLine("GET /file_name/{id}")
  String fileName(@Param("id") long id);

  /**
   * Получит имя файла по уникальной ссылке(коду).
   * Если указанного файла не найдется, то будет ошибка
   * @param key
   * @return
   */
  @RequestLine("GET /file_name_by_key?key={key}")
  String fileNameByLink(@Param("key") String key);

  @RequestLine("GET /file_by_key?key={key}")
  Response downloadFileByLink(@Param("key") String key);

  @RequestLine("GET /file_public/{id}")
  Response downloadFilePublic(@Param("id") long id);

  /**
   * Только для владельца файла
   * @param id
   * @return
   */
  @RequestLine("GET /file/{id}")
  Response downloadFile(@Param("id") long id);

  @RequestLine("GET /file_protected/{id}")
  Response downloadProtectedFile(@Param("id") long id);

  @RequestLine("GET /directories/{directory}")
  List<DirectoryData> getDirectories(@Param("directory") long directory);

  @RequestLine("GET /directories/0")
  List<DirectoryData> getRootDirectories();

  @RequestLine("GET /directory/{directory}/files?type={type}")
  List<FileData> getFiles(@Param("directory") long directory, @Param("type") FileType type);

  @RequestLine("GET /directory/0/files")
  List<FileData> getRootFiles();

  @RequestLine("GET /directory/{directory}/files")
  List<FileData> getFilesAllType(@Param("directory") long directory);

  @RequestLine("PUT /directory/{directory}/change_access_type/{type}")
  void changeDirAccessType(@Param("type") String type, @Param("directory") long directory);

  @RequestLine("PUT /file/{file}/change_access_type/{type}")
  void changeFileAccessType(@Param("type") String type, @Param("file") long file);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /directories/change_access_type/{type}")
  void changeDirAccessType(@Param("type") String type, List<Long> dirs);

  /**
   * Поменяет тип доступа файлам и вернет список приватных ссылок по ID файла, если было изменение на тип ссылки приватной
   * @param type
   * @param files
   * @return
   */
  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /files/change_access_type/{type}")
  Map<Long, Links>  changeFileAccessType(@Param("type") String type, List<Long> files);

  @RequestLine("PUT /directory/{directory}/move_to/directory/{parent}")
  void moveDirectory(@Param("directory") long directory, @Param("parent") long parent);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /directories/move_to/directory/{parent}")
  void moveDirectories(List<Long> directories, @Param("parent") long parent);

  @RequestLine("PUT /file/{file}/move_to/directory/{parent}")
  void moveFile(@Param("file") long file, @Param("parent") long parent);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("PUT /files/move_to/directory/{parent}")
  void moveFiles(List<Long> files, @Param("parent") long parent);

  @RequestLine("DELETE /directory/{directory}")
  void deleteDir(@Param("directory")long directory);


  @RequestLine("DELETE /file/{file}")
  void deleteFile(@Param("file")long file);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("DELETE /files")
  void deleteFiles(List<Long> files);

  @Headers(value = {"Content-Type: application/json"})
  @RequestLine("DELETE /directories")
  void deleteDirs(List<Long> directories);

  @RequestLine("GET /backups")
  List<Date> getBackupList();

  //private val backupDateFormat: SimpleDateFormat = SimpleDateFormat("yyy_mm_dd_H_m")

  @RequestLine("GET /backup/{date}")
  byte[] getBackup(@Param("date") String date);

  @RequestLine("POST /directories?name={name}")
  DirectoryData createInRootDirectory(@Param("name") String name);

  @RequestLine("POST /directories/{parent}?name={name}")
  DirectoryData createDirectory(@Param("name") String name, @Param("parent") long parent);

  @RequestLine("PUT /directories/{dir}?name={name}")
  DirectoryData changeDirectoryName(@Param("name") String name,  @Param("dir") long parent);

  @RequestLine("PUT /files/{id}?name={name}")
  void renameFile(@Param("name")  String name, @Param("id")long id);

  @RequestLine("PUT /directories/{id}?name={name}")
  void renameDirectory(@Param("name")  String name, @Param("id") long id);

  @RequestLine("PUT /files/{id}/regenerate_private_link")
  String reloadPrivateLink(@Param("id") long id);
}
