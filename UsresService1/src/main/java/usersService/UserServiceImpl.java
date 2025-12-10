package usersService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.UserDto;
import api.services.UsersService;

@RestController
public class UserServiceImpl implements UsersService {

	@Autowired
	private UserRepository repo;
	@Override
	public List<UserDto> getUsers() {
		List<UserModel> models=repo.findAll();
		List<UserDto> dtos=new ArrayList<UserDto>();
		for(UserModel model: models) {
			dtos.add(convertModelToDto(model));
		}
		return dtos;
	}

	@Override
	public UserDto getUserByEmail(String email) {
		 UserModel model = repo.findByEmail(email);
		    if (model == null) {
		        return null; 
		    }
		    return convertModelToDto(model);
		}
	

	@Override
	public ResponseEntity<?> createAdmin(UserDto dto) {
		if(repo.findByEmail(dto.getEmail())==null){
			dto.setRole("ADMIN");
			UserModel model=convertDtoToModel(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(model));
		}else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin with passed eamil alredy exist");
		}
	}

	@Override
	public ResponseEntity<?> createUser(UserDto dto) {
		if(repo.findByEmail(dto.getEmail())==null){
			dto.setRole("USER");
			UserModel model=convertDtoToModel(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(model));
		}else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("User with passed eamil doesnt exist");
		}
	}

	@Override
	public ResponseEntity<?> updateUser(UserDto dto) {
		if(repo.findByEmail(dto.getEmail())!=null){
			repo.updateUser(dto.getEmail(), dto.getPassword(), dto.getRole());
			return ResponseEntity.status(HttpStatus.OK).body(dto);
		}else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("User with passed eamil deoesnt exist");
		}
	}
	
	public UserDto convertModelToDto(UserModel model) {
		return new UserDto(model.getEmail(),model.getPassword(),model.getRole());
	}
	
	public UserModel convertDtoToModel(UserDto dto) {
		return new UserModel(dto.getEmail(),dto.getPassword(),dto.getRole());
	}

}
