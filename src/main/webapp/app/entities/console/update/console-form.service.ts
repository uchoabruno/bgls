import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IConsole, NewConsole } from '../console.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IConsole for edit and NewConsoleFormGroupInput for create.
 */
type ConsoleFormGroupInput = IConsole | PartialWithRequiredKeyOf<NewConsole>;

type ConsoleFormDefaults = Pick<NewConsole, 'id'>;

type ConsoleFormGroupContent = {
  id: FormControl<IConsole['id'] | NewConsole['id']>;
  name: FormControl<IConsole['name']>;
  image: FormControl<IConsole['image']>;
  imageContentType: FormControl<IConsole['imageContentType']>;
};

export type ConsoleFormGroup = FormGroup<ConsoleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ConsoleFormService {
  createConsoleFormGroup(console: ConsoleFormGroupInput = { id: null }): ConsoleFormGroup {
    const consoleRawValue = {
      ...this.getFormDefaults(),
      ...console,
    };
    return new FormGroup<ConsoleFormGroupContent>({
      id: new FormControl(
        { value: consoleRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(consoleRawValue.name, {
        validators: [Validators.required],
      }),
      image: new FormControl(consoleRawValue.image),
      imageContentType: new FormControl(consoleRawValue.imageContentType),
    });
  }

  getConsole(form: ConsoleFormGroup): IConsole | NewConsole {
    return form.getRawValue() as IConsole | NewConsole;
  }

  resetForm(form: ConsoleFormGroup, console: ConsoleFormGroupInput): void {
    const consoleRawValue = { ...this.getFormDefaults(), ...console };
    form.reset(
      {
        ...consoleRawValue,
        id: { value: consoleRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ConsoleFormDefaults {
    return {
      id: null,
    };
  }
}
