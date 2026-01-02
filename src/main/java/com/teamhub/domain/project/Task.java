package com.teamhub.domain.project;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.domain.user.User;
import com.teamhub.enums.project.TaskPriority;
import com.teamhub.enums.project.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@SQLRestriction("is_deleted = false")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskLabel> taskLabels = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    public void addChecklistItem(ChecklistItem item) {
        this.checklistItems.add(item);
    }

    public void updateInfo(String title, String description, TaskPriority taskPrioity, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = taskPrioity;
        this.dueDate = dueDate;
    }

    public void addLabel(Label label) {
        TaskLabel taskLabel = TaskLabel.builder()
                .task(this)
                .label(label)
                .build();
        this.taskLabels.add(taskLabel);
    }

    public void removeLabel(Label label) {
        this.taskLabels.removeIf(tl -> tl.getLabel().getId().equals(label.getId()));
    }

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    public void assignTo(User assignee) {
        this.assignee = assignee;
    }

    public void changeOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
