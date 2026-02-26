import { Component, computed, inject, NgZone, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { combineLatest, filter, Observable, Subscription, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ParseLinks } from 'app/core/util/parse-links.service';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';
import { EntityArrayResponseType, ItemService } from '../service/item.service';
import { ItemDeleteDialogComponent } from '../delete/item-delete-dialog.component';
import { IItem } from '../item.model';
import { IUser } from 'app/entities/user/user.model';
import { IConsole } from 'app/entities/console/console.model';
import { UserService } from 'app/entities/user/service/user.service';
import { ConsoleService } from 'app/entities/console/service/console.service';

interface ItemFilters {
  ownerId?: number;
  lendedToId?: number;
  game?: string;
  consoleId?: number;
}

@Component({
  standalone: true,
  selector: 'jhi-item',
  templateUrl: './item.component.html',
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, InfiniteScrollDirective],
})
export class ItemComponent implements OnInit {
  subscription: Subscription | null = null;
  items?: IItem[];
  isLoading = false;
  showFilters = false;

  owners: IUser[] = [];
  lendedTos: IUser[] = [];
  consoles: IConsole[] = [];
  isLoadingOptions = false;

  sortState = sortStateSignal({});

  filters: ItemFilters = {
    ownerId: undefined,
    lendedToId: undefined,
    game: '',
    consoleId: undefined,
  };

  itemsPerPage = ITEMS_PER_PAGE;
  links: WritableSignal<{ [key: string]: undefined | { [key: string]: string | undefined } }> = signal({});
  hasMorePage = computed(() => !!this.links().next);
  isFirstFetch = computed(() => Object.keys(this.links()).length === 0);

  public router = inject(Router);
  protected itemService = inject(ItemService);
  protected activatedRoute = inject(ActivatedRoute);
  protected sortService = inject(SortService);
  protected parseLinks = inject(ParseLinks);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);
  protected userService = inject(UserService);
  protected consoleService = inject(ConsoleService);

  trackId = (_index: number, item: IItem): number => this.itemService.getItemIdentifier(item);

  ngOnInit(): void {
    this.loadFilterOptions();

    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.loadFiltersFromRoute()),
        tap(() => this.reset()),
        tap(() => this.load()),
      )
      .subscribe();
  }

  loadFilterOptions(): void {
    this.isLoadingOptions = true;

    this.userService.query({ size: 1000 }).subscribe({
      next: res => {
        this.owners = res.body ?? [];
        this.lendedTos = res.body ?? [];
      },
      error: () => {
        this.owners = [];
        this.lendedTos = [];
      },
    });

    this.consoleService.query({ size: 1000 }).subscribe({
      next: res => {
        this.consoles = res.body ?? [];
        this.isLoadingOptions = false;
      },
      error: () => {
        this.consoles = [];
        this.isLoadingOptions = false;
      },
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  applyFilters(): void {
    this.reset();
    this.navigateWithFilters();
  }

  clearFilters(): void {
    this.filters = {
      ownerId: undefined,
      lendedToId: undefined,
      game: '',
      consoleId: undefined,
    };
    this.applyFilters();
  }

  reset(): void {
    this.items = [];
    this.links.set({});
  }

  loadNextPage(): void {
    this.load();
  }

  delete(item: IItem): void {
    const modalRef = this.modalService.open(ItemDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.item = item;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.reset()),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(event);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected loadFiltersFromRoute(): void {
    const params = this.activatedRoute.snapshot.queryParams;
    this.filters = {
      ownerId: params['ownerId'] ? Number(params['ownerId']) : undefined,
      lendedToId: params['lendedToId'] ? Number(params['lendedToId']) : undefined,
      game: params['game'] || '',
      consoleId: params['consoleId'] ? Number(params['consoleId']) : undefined,
    };

    this.showFilters = Object.values(this.filters).some(value => value !== undefined && value !== null && value !== '');
  }

  protected navigateWithFilters(): void {
    const queryParams: any = {
      sort: this.sortService.buildSortParam(this.sortState()),
    };

    if (this.filters.ownerId) {
      queryParams['ownerId'] = this.filters.ownerId;
    }
    if (this.filters.lendedToId) {
      queryParams['lendedToId'] = this.filters.lendedToId;
    }
    if (this.filters.game && this.filters.game.trim() !== '') {
      queryParams['game'] = this.filters.game.trim();
    }
    if (this.filters.consoleId) {
      queryParams['consoleId'] = this.filters.consoleId;
    }

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams,
      });
    });
  }

  protected buildFilterParams(): any {
    const filterParams: any = {};

    if (this.filters.ownerId) {
      filterParams['ownerId'] = this.filters.ownerId;
    }
    if (this.filters.lendedToId) {
      filterParams['lendedToId'] = this.filters.lendedToId;
    }
    if (this.filters.game && this.filters.game.trim() !== '') {
      filterParams['game'] = this.filters.game.trim();
    }
    if (this.filters.consoleId) {
      filterParams['consoleId'] = this.filters.consoleId;
    }

    return filterParams;
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.items = dataFromBody;
  }

  protected fillComponentAttributesFromResponseBody(data: IItem[] | null): IItem[] {
    // If there is previous link, data is a infinite scroll pagination content.
    if (this.links().prev) {
      const itemsNew = this.items ?? [];
      if (data) {
        for (const d of data) {
          if (itemsNew.map(op => op.id).indexOf(d.id) === -1) {
            itemsNew.push(d);
          }
        }
      }
      return itemsNew;
    }
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    const linkHeader = headers.get('link');
    if (linkHeader) {
      this.links.set(this.parseLinks.parseAll(linkHeader));
    } else {
      this.links.set({});
    }
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const queryObject: any = {
      size: this.itemsPerPage,
      eagerload: true,
    };

    const filterParams = this.buildFilterParams();
    Object.assign(queryObject, filterParams);

    if (this.hasMorePage()) {
      Object.assign(queryObject, this.links().next);
    } else if (this.isFirstFetch()) {
      Object.assign(queryObject, { sort: this.sortService.buildSortParam(this.sortState()) });
    }

    return this.itemService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(sortState: SortState): void {
    this.links.set({});
    this.sortState.set(sortState);
    this.navigateWithFilters();
  }
}
