import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Matiere, CreateMatiereRequest, UpdateMatiereRequest, MatiereSearchRequest } from '../models/matiere.model';

@Injectable({
  providedIn: 'root'
})
export class MatiereService {
  private apiUrl = `${environment.BASE_URL}/api/matieres`;

  constructor(private http: HttpClient) {}

  /**
   * Get all matieres with optional pagination
   */
  getAllMatieres(page: number = 0, size: number = 100): Observable<Matiere[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Matiere[]>(this.apiUrl, { params });
  }

  /**
   * Get matiere by ID
   */
  getMatiereById(id: number): Observable<Matiere> {
    return this.http.get<Matiere>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create a new matiere
   */
  createMatiere(request: CreateMatiereRequest): Observable<Matiere> {
    return this.http.post<Matiere>(this.apiUrl, request);
  }

  /**
   * Update an existing matiere
   */
  updateMatiere(id: number, request: UpdateMatiereRequest): Observable<Matiere> {
    return this.http.put<Matiere>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete a matiere
   */
  deleteMatiere(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Search matieres
   */
  searchMatieres(searchRequest: MatiereSearchRequest): Observable<Matiere[]> {
    let params = new HttpParams();
    
    if (searchRequest.nom) {
      params = params.set('nom', searchRequest.nom);
    }
    if (searchRequest.domaine) {
      params = params.set('domaine', searchRequest.domaine);
    }
    if (searchRequest.niveau) {
      params = params.set('niveau', searchRequest.niveau);
    }
    
    return this.http.get<Matiere[]>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get matieres by domain
   */
  getMatieresByDomaine(domaine: string): Observable<Matiere[]> {
    return this.http.get<Matiere[]>(`${this.apiUrl}/domaine/${domaine}`);
  }
}
