package com.example.smartcampus.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;

// ✅ NUEVAS CLASES: Clase para construir las búsquedas con Specification
public class UserSpecification {

    // ✅ Búsqueda por nombre, email O carrera
    public static Specification<User> searchByNameOrEmail(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction(); // Si no hay búsqueda, devuelve true
            }

            String searchPattern = "%" + search.toLowerCase() + "%";

            // Busca en fullName O email O nombre de carrera
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern)
            );
        };
    }

    // ✅ FILTRO POR CARRERA (agregado para filtros)
    public static Specification<User> filterByCareer(String career) {
        return (root, query, criteriaBuilder) -> {
            if (career == null || career.isEmpty()) {
                return criteriaBuilder.conjunction(); // Si no hay filtro, devuelve true
            }

            try {
                Integer careerId = Integer.parseInt(career);
                return criteriaBuilder.equal(root.get("careerId"), careerId);
            } catch (NumberFormatException e) {
                // Si la carrera no es un número válido, no filtra
                return criteriaBuilder.conjunction();
            }
        };
    }

    // ✅ FILTRO POR ROL (agregado para filtros)
    public static Specification<User> filterByRole(String role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null || role.isEmpty()) {
                return criteriaBuilder.conjunction(); // Si no hay filtro, devuelve true
            }

            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                return criteriaBuilder.equal(root.get("role"), roleEnum);
            } catch (IllegalArgumentException e) {
                // Si el rol no es válido, no filtra
                return criteriaBuilder.conjunction();
            }
        };
    }

    // ✅ FILTRO POR ESTADO (agregado para filtros)
    public static Specification<User> filterByStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction(); // Si no hay filtro, devuelve true
            }

            try {
                Status statusEnum = Status.valueOf(status.toUpperCase());
                return criteriaBuilder.equal(root.get("status"), statusEnum);
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, no filtra
                return criteriaBuilder.conjunction();
            }
        };
    }
}

