package SYSC4806.Controller;

import SYSC4806.Model.Category;
import SYSC4806.Model.Course;
import SYSC4806.Model.LearningOutcome;
import SYSC4806.Model.Program;
import SYSC4806.Repository.CategoryRepository;
import SYSC4806.Repository.CourseRepository;
import SYSC4806.Repository.LearningOutcomeRepository;
import SYSC4806.Repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import SYSC4806.Model.RequestWrapper;
import java.math.BigInteger;
import java.util.*;

@Controller
public class HomeController {

    private final CategoryRepository categoryRepository;

    private final CourseRepository courseRepository;

    private final LearningOutcomeRepository learningOutcomeRepository;

    private final ProgramRepository programRepository;

    @Autowired
    public HomeController(CategoryRepository categoryRepository, CourseRepository courseRepository, LearningOutcomeRepository learningOutcomeRepository, ProgramRepository programRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.learningOutcomeRepository = learningOutcomeRepository;
        this.programRepository = programRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("learningoutcomes", learningOutcomeRepository.findAll());
        model.addAttribute("programs", programRepository.findAll());
        return "index";
    }

    /*
     * Selects courses based on chosen program, year, courses and catagories and returns them to view
     * to refresh results table
     */
    @GetMapping("/results/{program}/{year}")
    public String showResults(Model model, @PathVariable("program") String p, @PathVariable("year") String year,
                              @RequestParam(value = "courses", required = false) String co,
                              @RequestParam(value = "categories", required = false) String ca) {
        long p_id = programRepository.findByName(p).getId();
        List<String> courseNames = courseRepository.findCourseByProgramAndYear(p_id, Integer.parseInt(year));

        if (co != null && !(co.isEmpty())) {
            List<String> courseNamesFilter = Arrays.asList(co.split(","));
            courseNames.retainAll(courseNamesFilter);
        }

        if (ca != null && !(ca.isEmpty())) {
            List<String> catNamesFilter = Arrays.asList(ca.split(","));

            //As per the category, finding the list of learning outcomes
            ArrayList<Long> loListId = new ArrayList<>();
            for (String cName : catNamesFilter) {
                List<BigInteger> loIdList = learningOutcomeRepository.findByCategoryName(cName);
                for (BigInteger loID : loIdList) {
                    loListId.add(loID.longValue());
                }
            }

            //As per the list of learning outcomes, find the list of course id's
            HashSet<BigInteger> courseIdsList = new HashSet<>();
            for (Long lo : loListId) {
                List<BigInteger> courseIds = learningOutcomeRepository.findByLearningOutcomeId(lo);
                courseIdsList.addAll(courseIds);
            }

            //As per course ids, find the course names
            ArrayList<String> coursesCatFilter = new ArrayList<>();
            for (BigInteger courseId : courseIdsList) {
                Course c = courseRepository.findById(courseId.longValue()).orElse(null);
                coursesCatFilter.add(c.getName());
            }

            courseNames.retainAll(coursesCatFilter);
        }

        ArrayList<Course> courses = new ArrayList<>();
        for (String cName : courseNames) {
            Course c = courseRepository.findByName(cName);
            courses.add(c);
        }
        model.addAttribute("courses", courses);
        return "fragments/results :: resultsTable";
    }

    /**
     * Request Mapping for handling adding a new course entry in the admin table
     * @param model Model to add attributes and update view
     * @param requestWrapper The wrapper object that holds a Course, Category, LearningOutcome, and Program
     * @return The request wrapper and a valid http status
     */
    @RequestMapping(value="addCourse", method=RequestMethod.POST, headers = "Content-Type=application/json")
    public String addCourse(Model model, @RequestBody RequestWrapper requestWrapper) {
        // Get all the submitted info from the wrapper
        Course course = requestWrapper.getCourse();
        ArrayList<String> loList = requestWrapper.getLearningOutcomeList();
        ArrayList<String> programList = requestWrapper.getProgramList();
        // Build the model relationships
        for (String s: loList){
            course.addLO(learningOutcomeRepository.findByName(s));
        }
        for(String s: programList){
            programRepository.findByName(s).addCourse(course);
        }

        // Save the new object to the db
        courseRepository.save(course);
        return "fragments/adminResults :: resultsTable";
    }

    @RequestMapping(value="addCategory", method=RequestMethod.POST, headers = "Content-Type=application/json")
    public String addCategory(Model model, @RequestBody RequestWrapper requestWrapper) {
        Category category = requestWrapper.getCategory();
        categoryRepository.save(category);

        return "fragments/adminResults :: resultsTable";
    }

    @RequestMapping(value="addLO", method=RequestMethod.POST, headers = "Content-Type=application/json")
    public String addLO(Model model, @RequestBody RequestWrapper requestWrapper) {
        LearningOutcome lo = requestWrapper.getLearningOutcome();
        lo.setCategory(categoryRepository.findByName(requestWrapper.getCategory().getName()));
        learningOutcomeRepository.save(lo);
        System.out.println(lo.getCategory().getName());
        return "fragments/adminResults :: resultsTable";
    }

    @RequestMapping(value="addProgram", method=RequestMethod.POST, headers = "Content-Type=application/json")
    public String addProgram(Model model, @RequestBody RequestWrapper requestWrapper) {
        Program program = requestWrapper.getProgram();
        ArrayList<String> list = requestWrapper.getCourseList();
        for (String s: list){
            program.addCourse(courseRepository.findByName(s));
        }
        programRepository.save(program);

        return "fragments/adminResults :: resultsTable";
    }

    /*
     * Request Mapping for handling deletion on selected course ID
     * @param String id to find in database
     * @return the new table without the deleted id
     */
    @RequestMapping(value = "/delete_entity/{id}", method = RequestMethod.GET)
    public String deleteData(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return "redirect:/admin";
    }

    /*
    *   Selects years that exists for a given program
    *   @param p Name of program selected
    *   @return List of years
     */
    @GetMapping("/results/{program}")
    public ResponseEntity<Object> getYears(@PathVariable("program") String p) {
        List<Course> courses = programRepository.findByName(p).getCourses();
        HashSet<Integer> years = new HashSet<>();
        for(Course c: courses) {
            years.add(c.getYear());
        }
        return new ResponseEntity<Object>(years, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public String getAdmin(Model model)
    {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("learningoutcomes", learningOutcomeRepository.findAll());
        model.addAttribute("programs", programRepository.findAll());
        return "admin";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }
}
