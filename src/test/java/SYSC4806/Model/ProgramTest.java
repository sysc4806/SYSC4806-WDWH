package SYSC4806.Model;

import SYSC4806.Repository.ProgramRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the Program Model class to ensure that the courses persist in the database.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProgramTest {

    /**
     * Repository used to save data about programs
     */
    @Autowired
    private ProgramRepository programRepository;

    /**
     * Setting up dummy information for programs
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Deleting the saved information for programs
     */
    @After
    public void tearDown() throws Exception {

    }

    @Test
    @Transactional
    public void contextLoads(){
        assertNotNull(programRepository);
    }

    @Test
    @Transactional
    public void testFindByName(){
        Program prog1 = programRepository.findByName("Software Engineering");
        assertEquals(prog1.getName(), "Software Engineering");
    }

    @Test
    @Transactional
    public void testNumberOfCourses(){
        Program prog1 = programRepository.findByName("Software Engineering");
        assertEquals(prog1.getCourses().size(), 6);
    }

}