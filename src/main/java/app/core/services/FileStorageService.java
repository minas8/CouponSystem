package app.core.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.core.exceptions.FileStorageException;

@Service
public class FileStorageService {

	@Value("${file.upload.dir}")
	private String storageDir;
	private Path fileStoragePath;

	@PostConstruct
	public void init() throws FileStorageException {

		this.fileStoragePath = Paths.get(this.storageDir).toAbsolutePath();

		// Create the directory if does not exist
		try {
			Files.createDirectories(fileStoragePath);
		} catch (IOException e) {
			throw new FileStorageException("Could not create directory.", e);
		}
	}

	public String storeFile(MultipartFile file) throws FileStorageException {

		String fileName = file.getOriginalFilename();
		if (fileName.contains("..")) {
			throw new FileStorageException("File name contains illegal characters. [fileName: " + fileName + "]");
		}

		// copy the file to the destination directory (if already exists replace)
		try {
			fileName = fileName.toLowerCase();
			Path targetLocation = this.fileStoragePath.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return fileName;
		} catch (IOException e) {
			throw new FileStorageException("storeFile failed. [fileName: " + fileName + "]", e);
		}
	}

}
