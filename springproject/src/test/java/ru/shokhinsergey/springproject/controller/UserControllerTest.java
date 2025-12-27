package ru.shokhinsergey.springproject.controller;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import ru.shokhinsergey.springproject.dto.UserDtoCreateAndUpdate;
import ru.shokhinsergey.springproject.dto.UserDtoResult;
import ru.shokhinsergey.springproject.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@Disabled
@WebMvcTest(UserController.class)
public class UserControllerTest {
    private final Integer ID = 1;
    private final Integer BAD_ID = 0;
    private UserDtoResult responseDtoUser;
    private UserDtoCreateAndUpdate requestDtoUser;

    private String requestValidJson;
    private String requestNotValidDataFromJson;


    @Value("${springproject.response.exception.not-found}")
    private String RESPONSE_NOT_FOUND;
    @Value("${springproject.response.exception.not-unique}")
    private String RESPONSE_NOT_UNIQUE;
    @Value("${springproject.response.exception.bad-argument}")
    private String RESPONSE_BAD_ARGUMENT;
    @Value("${springproject.response.exception.bad-id}")
    private String RESPONSE_BAD_ID;

    @MockitoBean
    private UserService mockService;
    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @PostConstruct
    private void init() throws Exception {
        requestDtoUser = new UserDtoCreateAndUpdate("Alex", "alex@mail.ru", 18);

        requestValidJson = requestDtoUser.toString();

        requestNotValidDataFromJson = """
                {
                "name": "Alex",
                "email": "alexmail.ru",
                "age": 18
                }
                """;

        responseDtoUser = UserDtoResult.Builder
                .builder()
                .setName("Alex")
                .setEmail("alex@mail.ru")
                .setAge(18)
                .setId(ID)
                .setCreated_At(LocalDate.now())
                .build();

    }

    @Test
    @DisplayName("Ответ метода \"GET\", если указанный \"id\" есть в базе данных.")
    void ifCorrectId_Ok_MethodGet() throws Exception {

        Mockito.doReturn(Optional.of(responseDtoUser)).when(mockService).get(ID);

        var mockMvcGet = mockMvc.perform(get("/users/{id}", ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));

        checkResponseBodyIfOk(mockMvcGet);


        Mockito.verify(mockService, Mockito.times(1)).get(ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    private void checkResponseBodyIfOk(ResultActions action) throws Exception {

        action
//                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name").value(responseDtoUser.getName()))
                .andExpect(jsonPath("$.email").value(responseDtoUser.getEmail()))
                .andExpect(jsonPath("$.age").value(responseDtoUser.getAge()))
                .andExpect(jsonPath("$.id").value(responseDtoUser.getId()))
                .andExpect(jsonPath("$.created_At").value(responseDtoUser.getCreated_At().toString()));
    }

    @Test
    @DisplayName("Ответ метода \"GET\", если указанный \"id\" отсутствует в базе данных.")
    void ifNotExistId_Exception_MethodGet() throws Exception {

        Mockito.doReturn(Optional.empty()).when(mockService).get(ID);

        var mockMvcGet = mockMvc.perform(get("/users/{id}", ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));

        checkResponseIfNotExistId(mockMvcGet);

        Mockito.verify(mockService, Mockito.times(1)).get(ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    private void checkResponseIfNotExistId(ResultActions action) throws Exception {
        action
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(RESPONSE_NOT_FOUND));
    }


    @Test
    @DisplayName("Ответ метода \"GET\", если указанный \"id\" не проходит валидацию.")
    void ifNotValidId_Exception_MethodsGet() throws Exception {

        var mockMvcGet = mockMvc.perform(get("/users/{id}", BAD_ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
        checkResponseIfNotValidId(mockMvcGet);
    }

    private void checkResponseIfNotValidId(ResultActions action) throws Exception {

        action
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(RESPONSE_BAD_ID));

        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"DELETE\", если указанный \"id\" есть в базе данных.")
    void ifCorrectId_Ok_MethodDelete() throws Exception {

        Mockito.doReturn(Optional.of(responseDtoUser)).when(mockService).delete(ID);

        var mockMvcDelete = mockMvc.perform(delete("/users/{id}", ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));

        checkResponseBodyIfOk(mockMvcDelete);

        Mockito.verify(mockService, Mockito.times(1)).delete(ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"DELETE\", если указанный \"id\" отсутствует в базе данных.")
    void ifNotExistId_Exception_MethodDelete() throws Exception {

        Mockito.doReturn(Optional.empty()).when(mockService).delete(ID);

        var mockMvcDelete = mockMvc.perform(delete("/users/{id}", ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));

        checkResponseIfNotExistId(mockMvcDelete);

        Mockito.verify(mockService, Mockito.times(1)).delete(ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"DELETE\", если указанный \"id\" не проходит валидацию.")
    void ifNotValidId_Exception_MethodsDelete() throws Exception {

        var mockMvcDelete = mockMvc.perform(delete("/users/{id}", BAD_ID).contentType(
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
        checkResponseIfNotValidId(mockMvcDelete);
    }


    @Test
    @DisplayName("Ответ метода \"UPDATE\", если указанный \"id\" есть в базе данных и корректный запрос.")
    void ifCorrectIdAndRequest_Ok_MethodUpdate() throws Exception {

        Mockito.doReturn(Optional.of(responseDtoUser)).when(mockService).update(requestDtoUser, ID);

        var mockMvcUpdate = mockMvc.perform(put("/users/{id}", ID).contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseBodyIfOk(mockMvcUpdate);

        Mockito.verify(mockService, Mockito.times(1)).update(requestDtoUser, ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"UPDATE\", если указанный \"id\" не проходит валидацию.")
    void ifNotValidId_Exception_MethodsUpdate() throws Exception {

        var mockMvcPut = mockMvc.perform(put("/users/{id}", BAD_ID).contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseIfNotValidId(mockMvcPut);
    }

    @Test
    @DisplayName("Ответ метода \"UPDATE\", если указанный \"id\" отсутствует в базе данных.")
    void ifNotExistId_Exception_MethodUpdate() throws Exception {

        Mockito.doReturn(Optional.empty()).when(mockService).update(requestDtoUser, ID);

        var mockMvcPut = mockMvc.perform(put("/users/{id}", ID).contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseIfNotExistId(mockMvcPut);

        Mockito.verify(mockService, Mockito.times(1)).update(requestDtoUser, ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"UPDATE\", если данные из запроса не проходят валидацию.")
    void ifNotValidArguments_Exception_MethodUpdate() throws Exception {


        var mockMvcPut = mockMvc.perform(put("/users/{id}", ID).contentType(
                MediaType.APPLICATION_JSON).content(requestNotValidDataFromJson));

        checkResponseIfNotValidArguments(mockMvcPut);
    }

    //На примере одного неверного аргумента
    private void checkResponseIfNotValidArguments(ResultActions action) throws Exception {

        action
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(RESPONSE_BAD_ARGUMENT));

        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"UPDATE\", если данные из запроса (\"email\") нарушают уникальность.")
    void ifNotUniqueEmailArgument_Exception_MethodUpdate() throws Exception {

        Mockito.doThrow(new DataIntegrityViolationException(RESPONSE_NOT_UNIQUE)).when(mockService)
                .update(requestDtoUser, ID);

        var mockMvcPut = mockMvc.perform(put("/users/{id}", ID).contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseIfNotUniqueArguments(mockMvcPut);

        Mockito.verify(mockService, Mockito.times(1)).update(requestDtoUser, ID);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    private void checkResponseIfNotUniqueArguments(ResultActions action) throws Exception {

        action
//                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string(RESPONSE_NOT_UNIQUE));
    }

    @Test
    @DisplayName("Ответ метода \"CREATE\", если корректный запрос.")
    void ifCorrectRequest_Ok_MethodCreate() throws Exception {

        Mockito.doReturn(responseDtoUser).when(mockService).create(requestDtoUser);

        var mockMvcPost = mockMvc.perform(post("/users").contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseBodyIfOk(mockMvcPost);

        Mockito.verify(mockService, Mockito.times(1)).create(requestDtoUser);
        Mockito.verifyNoMoreInteractions(mockService);
    }

    @Test
    @DisplayName("Ответ метода \"CREATE\", если данные из запроса не проходят валидацию.")
    void ifNotValidArguments_Exception_MethodCreate() throws Exception {


        var mockMvcPost = mockMvc.perform(post("/users").contentType(
                MediaType.APPLICATION_JSON).content(requestNotValidDataFromJson));

        checkResponseIfNotValidArguments(mockMvcPost);
    }

    @Test
    @DisplayName("Ответ метода \"CREATE\", если данные из запроса (\"email\") нарушают уникальность.")
    void ifNotUniqueEmailArgument_Exception_MethodCreate() throws Exception {

        Mockito.doThrow(new DataIntegrityViolationException(RESPONSE_NOT_UNIQUE)).when(mockService)
                .create(requestDtoUser);

        var mockMvcPost = mockMvc.perform(post("/users").contentType(
                MediaType.APPLICATION_JSON).content(requestValidJson));

        checkResponseIfNotUniqueArguments(mockMvcPost);

        Mockito.verify(mockService, Mockito.times(1)).create(requestDtoUser);
        Mockito.verifyNoMoreInteractions(mockService);

    }
}
