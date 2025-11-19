import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { SubjectService } from '../../../services/subject.service';
import { 
  Subject as SubjectModel, 
  CreateSubjectRequest, 
  UpdateSubjectRequest,
  NiveauAcademique,
  SubjectFormOptions,
  SubjectStatistics
} from '../../../core/models/subject.model';

@Component({
  selector: 'app-subject-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './subject-list.component.html',
  styleUrls: ['./subject-list.component.scss']
})
export class SubjectListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  // Data
  subjects: SubjectModel[] = [];
  filteredSubjects: SubjectModel[] = [];
  statistics: SubjectStatistics | null = null;
  formOptions: SubjectFormOptions | null = null;
  
  // UI State
  loading = false;
  error: string | null = null;
  
  // Modal states
  showCreateModal = false;
  showEditModal = false;
  showDeleteModal = false;
  showDetailModal = false;
  selectedSubject: SubjectModel | null = null;
  
  // Search and filtering
  searchForm!: FormGroup;
  filterForm!: FormGroup;
  
  // Forms
  createForm!: FormGroup;
  editForm!: FormGroup;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalSubjects = 0;
  
  // Enums for template
  NiveauAcademique = NiveauAcademique;
  
  // Expose global objects for template
  Object = Object;
  Math = Math;

  constructor(private subjectService: SubjectService) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.initializeComponent();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForms(): void {
    // Search form
    this.searchForm = new FormGroup({
      searchQuery: new FormControl('')
    });

    // Filter form
    this.filterForm = new FormGroup({
      domaine: new FormControl(''),
      niveau: new FormControl(''),
      sortBy: new FormControl('nom')
    });

    // Create form
    this.createForm = new FormGroup({
      nom: new FormControl('', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]),
      description: new FormControl('', [Validators.maxLength(1000)]),
      niveau: new FormControl(NiveauAcademique.DEBUTANT, [Validators.required]),
      domaine: new FormControl('', [Validators.required, Validators.maxLength(100)])
    });

    // Edit form
    this.editForm = new FormGroup({
      nom: new FormControl('', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]),
      description: new FormControl('', [Validators.maxLength(1000)]),
      niveau: new FormControl('', [Validators.required]),
      domaine: new FormControl('', [Validators.required, Validators.maxLength(100)])
    });
  }

  private initializeComponent(): void {
    this.setupSubscriptions();
    this.loadFormOptions();
    this.loadSubjects();
    this.loadStatistics();
  }

  private setupSubscriptions(): void {
    // Subscribe to service observables
    this.subjectService.subjects$
      .pipe(takeUntil(this.destroy$))
      .subscribe(subjects => {
        this.subjects = subjects;
        this.applyFilters();
      });

    this.subjectService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => this.loading = loading);

    this.subjectService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => this.error = error);

    // Setup search subscription
    this.searchForm.get('searchQuery')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(searchValue => {
        if (searchValue && searchValue.length >= 2) {
          this.searchSubjects(searchValue);
        } else if (!searchValue) {
          this.loadSubjects();
        }
      });

    // Setup filter subscriptions
    this.filterForm.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  // Data loading methods
  loadSubjects(): void {
    this.subjectService.loadSubjects({ 
      page: this.currentPage, 
      size: this.pageSize 
    });
  }

  loadFormOptions(): void {
    this.subjectService.getFormOptions()
      .pipe(takeUntil(this.destroy$))
      .subscribe(options => {
        this.formOptions = options;
      });
  }

  loadStatistics(): void {
    this.subjectService.getSubjectStatistics()
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        this.statistics = stats;
      });
  }

  searchSubjects(searchTerm: string): void {
    this.subjectService.searchSubjects(searchTerm).subscribe();
  }

  // Filtering logic
  applyFilters(): void {
    let filtered = [...this.subjects];
    
    const domaine = this.filterForm.get('domaine')?.value;
    const niveau = this.filterForm.get('niveau')?.value;
    const sortBy = this.filterForm.get('sortBy')?.value;

    // Apply domain filter
    if (domaine) {
      filtered = filtered.filter(subject => subject.domaine === domaine);
    }

    // Apply level filter
    if (niveau) {
      filtered = filtered.filter(subject => subject.niveau === niveau);
    }

    // Apply sorting
    if (sortBy) {
      filtered.sort((a, b) => {
        const aValue = (a as any)[sortBy];
        const bValue = (b as any)[sortBy];
        
        if (typeof aValue === 'string' && typeof bValue === 'string') {
          return aValue.localeCompare(bValue);
        }
        
        return aValue > bValue ? 1 : -1;
      });
    }

    this.filteredSubjects = filtered;
    this.totalSubjects = filtered.length;
  }

  clearFilters(): void {
    this.searchForm.reset();
    this.filterForm.reset();
    this.filterForm.patchValue({ sortBy: 'nom' });
    this.loadSubjects();
  }

  // CRUD operations
  openCreateModal(): void {
    this.createForm.reset();
    this.createForm.patchValue({ niveau: NiveauAcademique.DEBUTANT });
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.createForm.reset();
  }

  createSubject(): void {
    if (this.createForm.valid) {
      const request: CreateSubjectRequest = this.createForm.value;
      
      this.subjectService.createSubject(request)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.closeCreateModal();
            this.loadStatistics();
          },
          error: (error) => {
            console.error('Error creating subject:', error);
          }
        });
    }
  }

  openEditModal(subject: SubjectModel): void {
    this.selectedSubject = subject;
    this.editForm.patchValue({
      nom: subject.nom,
      description: subject.description,
      niveau: subject.niveau,
      domaine: subject.domaine
    });
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedSubject = null;
    this.editForm.reset();
  }

  updateSubject(): void {
    if (this.editForm.valid && this.selectedSubject?.id) {
      const request: UpdateSubjectRequest = this.editForm.value;
      
      this.subjectService.updateSubject(this.selectedSubject.id, request)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.closeEditModal();
            this.loadStatistics();
          },
          error: (error) => {
            console.error('Error updating subject:', error);
          }
        });
    }
  }

  openDeleteModal(subject: SubjectModel): void {
    this.selectedSubject = subject;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.selectedSubject = null;
  }

  deleteSubject(): void {
    if (this.selectedSubject?.id) {
      this.subjectService.deleteSubject(this.selectedSubject.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.closeDeleteModal();
            this.loadStatistics();
          },
          error: (error) => {
            console.error('Error deleting subject:', error);
          }
        });
    }
  }

  openDetailModal(subject: SubjectModel): void {
    this.selectedSubject = subject;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedSubject = null;
  }

  // Utility methods
  getLevelDisplayName(niveau: NiveauAcademique): string {
    switch (niveau) {
      case NiveauAcademique.DEBUTANT:
        return 'Débutant';
      case NiveauAcademique.INTERMEDIAIRE:
        return 'Intermédiaire';
      case NiveauAcademique.AVANCE:
        return 'Avancé';
      default:
        return niveau;
    }
  }

  getLevelColor(niveau: NiveauAcademique): string {
    switch (niveau) {
      case NiveauAcademique.DEBUTANT:
        return 'bg-green-100 text-green-800';
      case NiveauAcademique.INTERMEDIAIRE:
        return 'bg-yellow-100 text-yellow-800';
      case NiveauAcademique.AVANCE:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  refreshData(): void {
    this.subjectService.refreshSubjects();
    this.loadStatistics();
  }

  clearError(): void {
    this.subjectService.clearError();
  }

  // Pagination methods
  nextPage(): void {
    this.currentPage++;
    this.loadSubjects();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadSubjects();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadSubjects();
  }
}