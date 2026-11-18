package io.github.adit12t.platform.userservice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Users and their posts, with hypermedia links so clients can navigate instead of building URLs. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository users;
    private final PostRepository posts;

    public UserController(UserRepository users, PostRepository posts) {
        this.users = users;
        this.posts = posts;
    }

    @GetMapping
    public CollectionModel<EntityModel<UserView>> all() {
        List<EntityModel<UserView>> items = users.findAll().stream().map(this::model).toList();
        return CollectionModel.of(items, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<UserView> one(@PathVariable Long id) {
        return model(find(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<UserView>> create(@Valid @RequestBody NewUser request) {
        User saved = users.save(new User(request.name().trim(), request.birthDate()));
        EntityModel<UserView> body = model(saved);
        return ResponseEntity.created(body.getRequiredLink("self").toUri()).body(body);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = find(id);
        posts.deleteByUserId(id);
        users.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posts")
    public CollectionModel<PostView> postsOf(@PathVariable Long id) {
        find(id);
        List<PostView> items = posts.findByUserIdOrderByCreatedAtDesc(id).stream().map(PostView::from).toList();
        return CollectionModel.of(items,
                linkTo(methodOn(UserController.class).postsOf(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).one(id)).withRel("user"));
    }

    @PostMapping("/{id}/posts")
    public ResponseEntity<PostView> addPost(@PathVariable Long id, @Valid @RequestBody NewPost request) {
        Post saved = posts.save(new Post(find(id), request.description().trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PostView.from(saved));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed.");
        Map<String, String> errors = new LinkedHashMap<>();
        e.getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    private User find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with id " + id));
    }

    private EntityModel<UserView> model(User user) {
        return EntityModel.of(UserView.from(user),
                linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).postsOf(user.getId())).withRel("posts"),
                linkTo(methodOn(UserController.class).all()).withRel("users"));
    }

    public record NewUser(@NotBlank @Size(max = 100) String name, @NotNull @Past LocalDate birthDate) {
    }

    public record NewPost(@NotBlank @Size(max = 500) String description) {
    }

    public record UserView(Long id, String name, LocalDate birthDate) {
        static UserView from(User user) {
            return new UserView(user.getId(), user.getName(), user.getBirthDate());
        }
    }

    public record PostView(Long id, String description, Instant createdAt) {
        static PostView from(Post post) {
            return new PostView(post.getId(), post.getDescription(), post.getCreatedAt());
        }
    }
}
