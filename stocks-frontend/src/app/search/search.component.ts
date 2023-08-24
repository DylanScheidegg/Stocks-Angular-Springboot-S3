import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';


@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent {
  arrKeys: any = [];
  arrData: any = [];
  arrDataPast: any = [];

  getRowIndices(): number[] {
    const numRows = Math.ceil(this.arrKeys.length / 6);
    return Array.from({ length: numRows }, (_, index) => index);
  }

  getKeysForRow(rowIndex: number): string[] {
    const startIndex = rowIndex * 6;
    const endIndex = Math.min(startIndex + 6, this.arrKeys.length);
    return this.arrKeys.slice(startIndex, endIndex);
  }

  getSearchInput(searchInput: string) {
    // Clear the array
    this.arrKeys = [];
    this.arrData = [];
    // Simple GET request with response type <any>
    this.http.get<any>('http://localhost:8080/GetData/' + searchInput).subscribe(data => {
      // Loop through the data and push the keys into an array
      this.arrData = data[0];
      this.arrDataPast = data[1];
      for (var key in this.arrData) {
        this.arrKeys.push(key);
      }
      console.log(this.arrKeys);
      // Keys in alphabetical order
      this.arrKeys.sort();
    })

  }

  constructor(private http: HttpClient) { }

}
