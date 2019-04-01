package SYSC4806.Model;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Course is a model class that represents a course a student can take.
 * A course belongs to many programs and a student should acquire certain learning outcomes by taking this course.
 */
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Min(value = 1, message = "Year should not be less than 1")
    @Max(value = 4, message = "Year should not be greater than 4")
    private int year;

    @NotNull(message = "Name cannot be null")
    private String name;

    @NotNull(message = "Code cannot be null")
    private String code;

    @ManyToMany(mappedBy = "courses",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<LearningOutcome> learningOutcomes = new ArrayList<>();

    @ManyToMany(
            mappedBy = "courses",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Program> programs = new ArrayList<>();

    public Course() {
    }

    public Course(int year, String name, String code) {
        this.year = year;
        this.name = name;
        this.code = code;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<LearningOutcome> getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(List<LearningOutcome> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public void addLO(LearningOutcome lo) {
        this.learningOutcomes.add(lo);
        lo.addCourse(this);
    }

    public void removeLO(LearningOutcome lo) {
        this.learningOutcomes.remove(lo);
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

    public void addProgram(Program program) {
        this.programs.add(program);
    }

    public void removeProgram(Program program) {
        this.programs.remove(program);
    }
}
