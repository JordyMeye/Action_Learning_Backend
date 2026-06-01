package fr.epita.factory;

import fr.epita.model.Student;
import fr.epita.util.Helper;


public class StudentFactory {


    public static Student createStudent( String name, String surname, String email, int age, String address){
      if (!Helper.isValidEmail(email)) return null ;

      if(Helper.isNullorEmpty(name)|| Helper.isNullorEmpty(surname))
            return null;

        return new Student.Builder().setName(name).setSurname(surname).setEmail(email).setAge(age).setAddress(address).build();
    }
}
