import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { PdsSearchResultModel } from '../models/pds-search-result.model';
import { PdsSearchSuggestionsModel } from '../models/pds-search-suggestions.model';
import { PdsAggregationsModel } from '../models/pds-aggregations.model';
import { PdsCategoryModel } from '../models/pds-category.model';
import { PdsGlobalAggregationsWrapperModel } from '../models/pds-global-aggregations.model';
import { PdsAggregationResultModel } from '../models/pds-aggregation-item.model';

import * as moment from 'moment';

import { environment } from '../../environments/environment';

@Injectable()
export class PdsSearchService {

  constructor(private http: HttpClient) { }

  searchForPattern(
    pattern: string,
    categoriesFilter: Array<PdsCategoryModel> = new Array<PdsCategoryModel>(),
    selectedDateFilter: PdsAggregationResultModel = null
    ): Observable<PdsSearchResultModel> {
    let query: string = this.constructFinalQuery(pattern, categoriesFilter, selectedDateFilter);
    const params = new HttpParams().set('q', query);
    return this.http.get<PdsSearchResultModel>(`${environment.sdsApiBaseUrl}/document`, { params });
  }

  constructFinalQuery(
    pattern: string,
    categoriesFilter: Array<PdsCategoryModel> = new Array<PdsCategoryModel>(),
    selectedDateFilter: PdsAggregationResultModel = null
    ): string {

    let queryCategoriesFilter: string = "";
    if (categoriesFilter != null) {
      let categoryFilterStringQuery: string = this.categoryFilterToStringQuery(categoriesFilter);
      if (categoryFilterStringQuery != "") {
        queryCategoriesFilter += " AND (" + categoryFilterStringQuery + ")";
      }
    }

    let dateFilter: string = "";
    if (selectedDateFilter != null && selectedDateFilter.pdsAggregationItem.filterValue != 0) {
      let categoryFilterStringQuery: string = this.computeDateFilterValue(selectedDateFilter.pdsAggregationItem.filterValue);
      if (categoryFilterStringQuery != "") {
        dateFilter += " AND (" + categoryFilterStringQuery + ")";
      }
    }

    return pattern + queryCategoriesFilter + dateFilter;
  }

  private computeDateFilterValue(monthToRemove: any): string {
    let fromDate: string = moment().subtract(monthToRemove, 'months').startOf("month").format('YYYY-MM-DD');
    let toDate: string = moment().endOf("month").format('YYYY-MM-DD'); // can be removed for '*' ?
    return "fileDate:[" + fromDate + " TO " + toDate + "]";
  }

  private categoryFilterToStringQuery(categoryFilter: Array<PdsCategoryModel>): string {
    return categoryFilter
    .map(function formatedQuery(c: PdsCategoryModel) {
      return "categoryId:" + c.id;
    })
    .join(" OR ");
  }

  getSuggestionsForPattern(pattern: string): Observable<PdsSearchSuggestionsModel> {
    const params = new HttpParams().set('q', pattern);
    return this.http.get<PdsSearchSuggestionsModel>(`${environment.sdsApiBaseUrl}/document/suggest`, {params});
  }

  getTopTermsRelatedToPattern(pattern: string): Observable<PdsAggregationsModel> {
    const params = new HttpParams().set('q', pattern);
    return this.http.get<PdsAggregationsModel>(`${environment.sdsApiBaseUrl}/document/top_terms`, {params});
  }

  getAggregationsForPattern(pattern: string): Observable<PdsGlobalAggregationsWrapperModel> {
    const params = new HttpParams().set('q', pattern);
    return this.http.get<PdsGlobalAggregationsWrapperModel>(`${environment.sdsApiBaseUrl}/document/aggregations`, {params});
  }

}