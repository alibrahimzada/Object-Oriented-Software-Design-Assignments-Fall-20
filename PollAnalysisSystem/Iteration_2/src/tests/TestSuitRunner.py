import sys
import os
sys.path.insert(1, os.getcwd() + '/src')

import unittest
from tests.AnswerTests import TestAnswerMethods
from tests.QuestionTests import TestQuestionMethods
from tests.AttendancePollTests import TestAttendancePollMethods
from tests.QuizPollTests import TestQuizPollMethods
from tests.CourseTests import TestCourseMethods
from tests.DepartmentTests import TestDepartmentMethods
from tests.InstructorTests import TestInstructorMethods
from tests.StudentTests import TestStudentMethods
from tests.PollSubmissionTests import TestPollSubmissionMethods

class TestSuitRunner():
    
    def __init__(self):
        self.__test_suite = self.__create_suite()
   
    @property
    def test_suite(self):
        return self.__test_suite

    def run_tests(self):
        runner = unittest.TextTestRunner()
        runner.run(self.__test_suite)

    def __create_suite(self):
        """
            Gather all the tests in a test suite.
        """
        test_suite = unittest.TestSuite()
        test_classes = [TestAnswerMethods, TestQuestionMethods, TestAttendancePollMethods,
                        TestQuizPollMethods, TestCourseMethods,TestDepartmentMethods,
                        TestInstructorMethods, TestStudentMethods, TestPollSubmissionMethods]
        for test_class in test_classes:
            test_suite.addTest(unittest.makeSuite(test_class))
        return test_suite

#run
# test_suite = TestSuitRunner().run_tests()



