import React, { forwardRef } from 'react';
import { DataTable as PrimeDataTable } from 'primereact/datatable/datatable.esm.js';
import { Dropdown as PrimeDropdown } from 'primereact/dropdown/dropdown.esm.js';
import { MultiSelect as PrimeMultiSelect } from 'primereact/multiselect/multiselect.esm.js';
import { AutoComplete as PrimeAutoComplete } from 'primereact/autocomplete/autocomplete.esm.js';
import { ListBox as PrimeListBox } from 'primereact/listbox/listbox.esm.js';

export const asCollection = (value) => {
    if (Array.isArray(value)) return value;
    if (!value || typeof value !== 'object') return [];
    if (Array.isArray(value.content)) return value.content;
    if (Array.isArray(value.data)) return value.data;
    if (value.data && typeof value.data === 'object') return asCollection(value.data);
    return [];
};

export const DataTable = forwardRef(function SafeDataTable({ value, frozenValue, ...props }, ref) {
    return (
        <PrimeDataTable
            ref={ref}
            {...props}
            value={asCollection(value)}
            frozenValue={frozenValue == null ? frozenValue : asCollection(frozenValue)}
        />
    );
});

export const Dropdown = forwardRef(function SafeDropdown({ options, ...props }, ref) {
    return <PrimeDropdown ref={ref} {...props} options={asCollection(options)} />;
});

export const MultiSelect = forwardRef(function SafeMultiSelect({ options, ...props }, ref) {
    return <PrimeMultiSelect ref={ref} {...props} options={asCollection(options)} />;
});

export const AutoComplete = forwardRef(function SafeAutoComplete({ suggestions, ...props }, ref) {
    return <PrimeAutoComplete ref={ref} {...props} suggestions={asCollection(suggestions)} />;
});

export const ListBox = forwardRef(function SafeListBox({ options, ...props }, ref) {
    return <PrimeListBox ref={ref} {...props} options={asCollection(options)} />;
});
