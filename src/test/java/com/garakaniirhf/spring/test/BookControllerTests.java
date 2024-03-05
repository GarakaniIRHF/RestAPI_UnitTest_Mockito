package com.garakaniirhf.spring.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garakaniirhf.spring.test.controller.BookController;
import com.garakaniirhf.spring.test.model.Book;
import com.garakaniirhf.spring.test.repository.BookRepository;

@WebMvcTest(BookController.class)
public class BookControllerTests {
	@MockBean
	private BookRepository bookRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldCreateBook() throws Exception {
		Book book = new Book("@WebMvcTest", "Comment", true);

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(book)))
		.andExpect(status().isCreated())
		.andDo(print());
	}

	@Test
	void shouldReturnBook() throws Exception {
		long id = 1L;
		Book book = new Book(id, "@WebMvcTest", "Comment", true);

		when(bookRepository.findById(id)).thenReturn(Optional.of(book));
		mockMvc.perform(get("/api/books/{id}", id)).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(id))
		.andExpect(jsonPath("$.caption").value(book.getCaption()))
		.andExpect(jsonPath("$.comment").value(book.getComment()))
		.andExpect(jsonPath("$.rated").value(book.isRated()))
		.andDo(print());
	}

	@Test
	void shouldReturnNotFoundBook() throws Exception {
		long id = 1L;

		when(bookRepository.findById(id)).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/books/{id}", id))
		.andExpect(status().isNotFound())
		.andDo(print());
	}


	@Test
	void shouldReturnListOfBooks() throws Exception {
		List<Book> books = new ArrayList<>(
				Arrays.asList(new Book(1, "Test 1", "Comment 1", true),
						new Book(2, "Test 2", "Comment 2", true),
						new Book(3, "Test 3", "Comment 3", true)));

		when(bookRepository.findAll()).thenReturn(books);
		mockMvc.perform(get("/api/books"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(books.size()))
		.andDo(print());
	}

	@Test
	void shouldReturnListOfBooksWithFilter() throws Exception {
		List<Book> books = new ArrayList<>(
				Arrays.asList(new Book(1, "Test", "Comment 1", true),
						new Book(3, "Sample", "Comment 2", true)));

		String caption = "999";
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("caption", caption);

		when(bookRepository.findByCaptionContaining(caption)).thenReturn(books);
		mockMvc.perform(get("/api/books").params(paramsMap))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(books.size()))
		.andDo(print());

	}

	@Test
	void shouldReturnNoContentWhenFilter() throws Exception {
		String caption = "Test";
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("caption", caption);

		List<Book> books = Collections.emptyList();

		when(bookRepository.findByCaptionContaining(caption)).thenReturn(books);
		mockMvc.perform(get("/api/books").params(paramsMap))
		.andExpect(status().isNoContent())
		.andDo(print());
	}


	@Test
	void shouldUpdateBook() throws Exception {
		long id = 1L;

		Book book = new Book(id, "Test", "Comment", false);
		Book updatedbook = new Book(id, "Updated", "CommentUpdated", true);

		when(bookRepository.findById(id)).thenReturn(Optional.of(book));
		when(bookRepository.save(any(Book.class))).thenReturn(updatedbook);

		mockMvc.perform(put("/api/books/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedbook)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.caption").value(updatedbook.getCaption()))
		.andExpect(jsonPath("$.comment").value(updatedbook.getComment()))
		.andExpect(jsonPath("$.rated").value(updatedbook.isRated()))
		.andDo(print());
	}


	@Test
	void shouldReturnNotFoundUpdateBook() throws Exception {
		long id = 1L;

		Book updatedbook = new Book(id, "Updated", "Updated", true);

		when(bookRepository.findById(id)).thenReturn(Optional.empty());
		when(bookRepository.save(any(Book.class))).thenReturn(updatedbook);

		mockMvc.perform(put("/api/books/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedbook)))
		.andExpect(status().isNotFound())
		.andDo(print());
	}

	@Test
	void shouldDeleteBook() throws Exception {
		long id = 1L;

		doNothing().when(bookRepository).deleteById(id);
		mockMvc.perform(delete("/api/books/{id}", id))
		.andExpect(status().isNoContent())
		.andDo(print());
	}

	@Test
	void shouldDeleteAllBooks() throws Exception {
		doNothing().when(bookRepository).deleteAll();
		mockMvc.perform(delete("/api/books"))
		.andExpect(status().isNoContent())
		.andDo(print());
	}

}
